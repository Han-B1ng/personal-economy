package com.personaleconomy.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WorkspaceVO(String source, String updatedAt, Profile profile, List<Goal> goals,
        List<Task> tasks, List<Event> events, List<Category> categories,
        List<FinanceRecord> financeRecords, Assets assets) {
    public record Profile(String nickname, String stockCode, String bio, String avatar) {}
    public record Goal(String id, String title, String categoryId, String description,
            LocalDate startDate, LocalDate dueDate, int progress, String status, LocalDate completedDate) {}
    public record Task(String id, String title, String description, LocalDate dueDate,
            String dueTime, String priority, String goalId, boolean completed) {}
    public record Event(String id, String title, String categoryId, String description,
            LocalDate date, String importance, String sourceGoalId, String evaluationStatus,
            BigDecimal contribution) {}
    public record Category(String id, String name, boolean system, boolean active) {}
    public record FinanceRecord(String id, String type, BigDecimal amount, String category,
            LocalDate date, String note) {}
    public record Assets(BigDecimal cash, BigDecimal other, BigDecimal debt, String updatedAt) {}
}
