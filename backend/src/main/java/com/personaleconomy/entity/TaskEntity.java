package com.personaleconomy.entity;

import java.time.LocalDate;

public record TaskEntity(String id, String title, String description, LocalDate dueDate, String dueTime, String priority, String goalId, boolean completed) {}
