package com.personaleconomy.mapper;

import com.personaleconomy.entity.*;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Parameterized SQL only; every query explicitly scopes the configured local user. */
@Mapper
public interface WorkspaceMapper {
    UserEntity selectUser(@Param("userId") String userId);
    StockEntity selectStock(@Param("userId") String userId);
    WorkspaceStateEntity selectState(@Param("userId") String userId);
    WorkspaceStateEntity lockState(@Param("userId") String userId);
    AssetSnapshotEntity selectAssets(@Param("userId") String userId);
    void insertUserIfAbsent(@Param("userId") String userId, @Param("row") UserEntity row);
    void insertStateIfAbsent(@Param("userId") String userId);
    void updateUser(@Param("userId") String userId, @Param("row") UserEntity row);
    void insertStock(@Param("userId") String userId, @Param("row") StockEntity row);
    void insertAssets(@Param("userId") String userId, @Param("row") AssetSnapshotEntity row);
    void finishImport(@Param("userId") String userId, @Param("source") String source, @Param("updatedAt") Instant updatedAt);
    void updateTimestamp(@Param("userId") String userId, @Param("updatedAt") Instant updatedAt);
    void deleteTask(@Param("userId") String userId, @Param("id") String id);
    void deleteEvent(@Param("userId") String userId, @Param("id") String id);
    List<GoalEntity> selectGoals(@Param("userId") String userId);
    void upsertGoal(@Param("userId") String userId, @Param("row") GoalEntity row);
    List<TaskEntity> selectTasks(@Param("userId") String userId);
    void upsertTask(@Param("userId") String userId, @Param("row") TaskEntity row);
    List<GrowthEventEntity> selectEvents(@Param("userId") String userId);
    void upsertEvent(@Param("userId") String userId, @Param("row") GrowthEventEntity row);
    List<EventCategoryEntity> selectCategories(@Param("userId") String userId);
    void insertCategory(@Param("userId") String userId, @Param("row") EventCategoryEntity row);
    void updateCategory(@Param("userId") String userId, @Param("row") EventCategoryEntity row);
    List<FinanceRecordEntity> selectFinanceRecords(@Param("userId") String userId);
    void upsertFinanceRecord(@Param("userId") String userId, @Param("row") FinanceRecordEntity row);
}
