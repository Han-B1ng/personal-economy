package com.personaleconomy.entity;

import java.time.LocalDate;
import java.math.BigDecimal;

public record FinanceRecordEntity(String id, String type, BigDecimal amount, String category, LocalDate date, String note) {}
