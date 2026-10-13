package com.personaleconomy.service.impl;

import com.personaleconomy.service.HomeQueryService;
import com.personaleconomy.service.WorkspaceService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personaleconomy.vo.HomeSnapshot;
import java.io.IOException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/** Read-only demonstration adapter, not a persistence or scoring implementation. */
@Service
@Profile("(bootstrap | mysql) & !prod")
public class DemoHomeQueryService implements HomeQueryService {
    private final HomeSnapshot snapshot;
    private final WorkspaceService workspace;

    public DemoHomeQueryService(ObjectMapper objectMapper, WorkspaceService workspace) throws IOException {
        this.workspace=workspace;
        try (var input = new ClassPathResource("fixtures/home-demo.json").getInputStream()) {
            snapshot = objectMapper.readValue(input, HomeSnapshot.class);
        }
        if (!"demo".equals(snapshot.source())) {
            throw new IllegalStateException("Bootstrap home data must be marked demo");
        }
    }

    @Override
    public HomeSnapshot getSnapshot() {
        var state=workspace.snapshot();
        var recent=state.events().stream().sorted(java.util.Comparator.comparing(com.personaleconomy.vo.WorkspaceVO.Event::date).reversed()).limit(3)
                .map(e -> new HomeSnapshot.RecentEvent(e.id(),e.title(),e.date(),
                        state.categories().stream().filter(c->c.id().equals(e.categoryId())).map(c->new String(Character.toChars(c.name().codePointAt(0)))).findFirst().orElse("记"),e.contribution())).toList();
        return new HomeSnapshot("demo",state.profile().nickname(),state.profile().stockCode(),snapshot.index(),snapshot.change(),snapshot.changePercent(),snapshot.history(),recent);
    }
}
