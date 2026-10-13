package com.personaleconomy.modules.home;

import com.personaleconomy.service.HomeQueryService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import com.personaleconomy.controller.HomeController;
import com.personaleconomy.service.impl.DemoHomeQueryService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("bootstrap")
class HomeQueryIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private HomeQueryService queryService;

    @Test
    void doesNotExposeLocalQueriesInTheProductionProfile() {
        for (String profiles : new String[] {"prod,mysql", "bootstrap,prod"}) {
            new ApplicationContextRunner()
                    .withUserConfiguration(HomeController.class, DemoHomeQueryService.class)
                    .withPropertyValues("spring.profiles.active=" + profiles)
                    .run(context -> {
                        assertThat(context).doesNotHaveBean(HomeController.class);
                        assertThat(context).doesNotHaveBean(DemoHomeQueryService.class);
                    });
        }
    }

    @Test
    void servesAnExplicitDemoSnapshotWithStableFrontendFields() throws Exception {
        mvc.perform(get("/api/v1/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.source").value("demo"))
                .andExpect(jsonPath("$.data.nickname").value("Han_B1ng"))
                .andExpect(jsonPath("$.data.stockCode").value("PE001"))
                .andExpect(jsonPath("$.data.index").value(1284.68))
                .andExpect(jsonPath("$.data.history", hasSize(43)))
                .andExpect(jsonPath("$.data.recentEvents", hasSize(3)))
                .andExpect(jsonPath("$.data.recentEvents[0].date").value("2026-10-12"));
    }

    @Test
    void doesNotOfferWritesOrAllowCallersToMutateDemoHistory() throws Exception {
        mvc.perform(post("/api/v1/home"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"));
        var snapshot = queryService.getSnapshot();
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class, snapshot.history()::clear);
        assertThat(queryService.getSnapshot().index()).isEqualByComparingTo(new BigDecimal("1284.68"));
    }

    @Test
    void historyIsChronologicalAndMatchesTheDisplayedLatestIndex() {
        var snapshot = queryService.getSnapshot();
        var history = snapshot.history();
        for (int i = 1; i < history.size(); i++) {
            assertThat(history.get(i).timestamp()).isGreaterThan(history.get(i - 1).timestamp());
        }
        assertThat(history.get(history.size() - 1).value()).isEqualByComparingTo(snapshot.index());
    }
}
