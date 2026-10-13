package com.personaleconomy.entity;

import java.math.BigDecimal;
import java.time.Instant;

public record AssetSnapshotEntity(BigDecimal cash, BigDecimal other, BigDecimal debt, Instant updatedAt) {}
