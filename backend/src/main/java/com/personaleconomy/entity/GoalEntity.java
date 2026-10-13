package com.personaleconomy.entity;

import java.time.LocalDate;

public record GoalEntity(String id, String title, String categoryId, String description, LocalDate startDate, LocalDate dueDate, int progress, String status, LocalDate completedDate) {}
