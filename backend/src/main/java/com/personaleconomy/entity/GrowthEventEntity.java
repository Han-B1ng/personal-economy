package com.personaleconomy.entity;

import java.time.LocalDate;
import java.math.BigDecimal;

public record GrowthEventEntity(String id, String title, String categoryId, String description, LocalDate date, String importance, String sourceGoalId, String evaluationStatus, BigDecimal contribution) {}
