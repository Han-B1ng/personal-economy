package com.personaleconomy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personaleconomy.common.exception.WorkspaceException;
import com.personaleconomy.entity.*;
import com.personaleconomy.mapper.WorkspaceMapper;
import com.personaleconomy.vo.WorkspaceMutationVO;
import com.personaleconomy.vo.WorkspaceVO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Relational local adapter. Shared domain rules run within an explicit database transaction. */
@Service
@Profile("mysql & !bootstrap & !prod")
public class MysqlWorkspaceService extends AbstractWorkspaceService {
    private final WorkspaceMapper sql;
    private final String userId;
    private final Path importFile;
    private final TransactionTemplate writeTransaction;
    private final TransactionTemplate readTransaction;

    public MysqlWorkspaceService(ObjectMapper mapper, WorkspaceMapper sql,
            PlatformTransactionManager transactionManager,
            @Value("${personal-economy.local-user-id}") String userId,
            @Value("${personal-economy.data-file}") String importFile) {
        super(mapper);
        this.sql = sql;
        this.userId = userId;
        this.importFile = Path.of(importFile).toAbsolutePath().normalize();
        writeTransaction = new TransactionTemplate(transactionManager);
        readTransaction = new TransactionTemplate(transactionManager);
        readTransaction.setReadOnly(true);
        readTransaction.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
    }

    /** Import exactly once. A successful existing database always takes precedence over JSON. */
    public void initialize() {
        writeTransaction.executeWithoutResult(transaction -> {
            var existing = sql.selectState(userId);
            if (existing != null && existing.initialized()) return;
            WorkspaceVO seed = readImport();
            sql.insertUserIfAbsent(userId, user(seed));
            sql.insertStateIfAbsent(userId);
            if (sql.lockState(userId).initialized()) return;
            sql.updateUser(userId, user(seed));
            sql.insertStock(userId, new StockEntity(seed.profile().stockCode()));
            persistDifference(null, seed);
            sql.finishImport(userId, Files.exists(importFile) ? "local-file" : "demo-fixture",
                    Instant.parse(seed.updatedAt()));
        });
    }

    private WorkspaceVO readImport() {
        try {
            WorkspaceVO seed;
            if (Files.exists(importFile)) {
                seed = mapper.readValue(importFile.toFile(), WorkspaceVO.class);
            } else try (var input = new ClassPathResource("fixtures/workspace-demo.json").getInputStream()) {
                seed = mapper.readValue(input, WorkspaceVO.class);
            }
            if (!"local-demo".equals(seed.source())) throw new IOException("Unsupported import source");
            return seed;
        } catch (IOException e) {
            throw new IllegalStateException("Workspace import failed; original JSON remains untouched", e);
        }
    }

    @Override public WorkspaceVO snapshot() {
        return readTransaction.execute(transaction -> readSnapshot());
    }

    private WorkspaceVO readSnapshot() {
        var state = sql.selectState(userId);
        if (state == null || !state.initialized()) {
            throw new WorkspaceException(HttpStatus.SERVICE_UNAVAILABLE, "数据库工作区尚未初始化");
        }
        var profile = sql.selectUser(userId);
        var stock = sql.selectStock(userId);
        var assets = sql.selectAssets(userId);
        return new WorkspaceVO("local-mysql", state.updatedAt().toString(),
                new WorkspaceVO.Profile(profile.nickname(), stock.stockCode(), profile.bio(), profile.avatar()),
                convert(sql.selectGoals(userId), WorkspaceVO.Goal.class),
                convert(sql.selectTasks(userId), WorkspaceVO.Task.class),
                convert(sql.selectEvents(userId), WorkspaceVO.Event.class),
                convert(sql.selectCategories(userId), WorkspaceVO.Category.class),
                convert(sql.selectFinanceRecords(userId), WorkspaceVO.FinanceRecord.class),
                new WorkspaceVO.Assets(assets.cash(), assets.other(), assets.debt(), assets.updatedAt().toString()));
    }

    @Override protected WorkspaceMutationVO change(String id, Consumer<ObjectNode> action) {
        return writeTransaction.execute(transaction -> {
            var state = sql.lockState(userId);
            if (state == null || !state.initialized()) {
                throw new WorkspaceException(HttpStatus.SERVICE_UNAVAILABLE, "数据库工作区尚未初始化");
            }
            WorkspaceVO before = readSnapshot();
            ObjectNode next = mapper.valueToTree(before);
            action.accept(next);
            next.put("updatedAt", Instant.now().toString());
            WorkspaceVO after = mapper.convertValue(next, WorkspaceVO.class);
            persistDifference(before, after);
            sql.updateTimestamp(userId, Instant.parse(after.updatedAt()));
            return new WorkspaceMutationVO(readSnapshot(), id);
        });
    }

    private UserEntity user(WorkspaceVO workspace) {
        var profile = workspace.profile();
        return new UserEntity(profile.nickname(), profile.bio(), profile.avatar());
    }

    /** Only changed rows are written; no truncate or full workspace replacement. */
    private void persistDifference(WorkspaceVO before, WorkspaceVO after) {
        if (before != null && !before.profile().equals(after.profile())) sql.updateUser(userId, user(after));
        sync(before == null ? List.of() : before.categories(), after.categories(), WorkspaceVO.Category::id,
                row -> {
                    var entity = mapper.convertValue(row, EventCategoryEntity.class);
                    boolean exists = before != null && before.categories().stream().anyMatch(c -> c.id().equals(row.id()));
                    if (exists) sql.updateCategory(userId, entity); else sql.insertCategory(userId, entity);
                }, null);
        sync(before == null ? List.of() : before.goals(), after.goals(), WorkspaceVO.Goal::id,
                row -> sql.upsertGoal(userId, mapper.convertValue(row, GoalEntity.class)), null);
        sync(before == null ? List.of() : before.tasks(), after.tasks(), WorkspaceVO.Task::id,
                row -> sql.upsertTask(userId, mapper.convertValue(row, TaskEntity.class)),
                id -> sql.deleteTask(userId, id));
        sync(before == null ? List.of() : before.events(), after.events(), WorkspaceVO.Event::id,
                row -> sql.upsertEvent(userId, mapper.convertValue(row, GrowthEventEntity.class)),
                id -> sql.deleteEvent(userId, id));
        sync(before == null ? List.of() : before.financeRecords(), after.financeRecords(), WorkspaceVO.FinanceRecord::id,
                row -> sql.upsertFinanceRecord(userId, mapper.convertValue(row, FinanceRecordEntity.class)), null);
        if (before == null || !sameAssets(before.assets(), after.assets())) {
            var assets = after.assets();
            sql.insertAssets(userId, new AssetSnapshotEntity(assets.cash(), assets.other(), assets.debt(),
                    Instant.parse(assets.updatedAt())));
        }
    }

    // DECIMAL JDBC values carry a scale (e.g. 8500.00); JSON normalization may remove it.
    // BigDecimal.equals is therefore unsuitable for detecting a financial value change.
    private boolean sameAssets(WorkspaceVO.Assets before, WorkspaceVO.Assets after) {
        return before.cash().compareTo(after.cash()) == 0
                && before.other().compareTo(after.other()) == 0
                && before.debt().compareTo(after.debt()) == 0
                && Instant.parse(before.updatedAt()).equals(Instant.parse(after.updatedAt()));
    }

    private <T> List<T> convert(List<?> rows, Class<T> type) {
        return rows.stream().map(row -> mapper.convertValue(row, type)).toList();
    }

    private <T> void sync(List<T> before, List<T> after, Function<T, String> key,
            Consumer<T> upsert, Consumer<String> delete) {
        Map<String, T> oldRows = before.stream().collect(Collectors.toMap(key, Function.identity()));
        for (T row : after) {
            T old = oldRows.remove(key.apply(row));
            if (old == null || !mapper.valueToTree(row).equals(mapper.valueToTree(old))) upsert.accept(row);
        }
        if (!oldRows.isEmpty() && delete == null) throw new IllegalStateException("Unsupported collection deletion");
        if (delete != null) oldRows.keySet().forEach(delete);
    }
}
