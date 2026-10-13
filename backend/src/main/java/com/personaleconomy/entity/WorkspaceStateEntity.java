package com.personaleconomy.entity;

import java.time.Instant;

public record WorkspaceStateEntity(boolean initialized, Instant updatedAt) {}
