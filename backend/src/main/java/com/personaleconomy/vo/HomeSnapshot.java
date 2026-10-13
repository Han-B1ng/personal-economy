package com.personaleconomy.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HomeSnapshot(
        String source,
        String nickname,
        String stockCode,
        BigDecimal index,
        BigDecimal change,
        BigDecimal changePercent,
        List<HistoryPoint> history,
        List<RecentEvent> recentEvents) {
    public HomeSnapshot {
        history = List.copyOf(history);
        recentEvents = List.copyOf(recentEvents);
    }

    public record HistoryPoint(long timestamp, BigDecimal value) {}

    public record RecentEvent(
            String id, String title, LocalDate date, String categoryMark,
            BigDecimal contribution) {}
}
