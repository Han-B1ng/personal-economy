package com.personaleconomy.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class WorkspaceRequests {
    private WorkspaceRequests() {}
    public record Goal(@NotBlank @Size(max=120) String title, @NotBlank String categoryId,
            @Size(max=2000) String description, @NotNull LocalDate startDate,
            @NotNull LocalDate dueDate, @Min(0) @Max(100) int progress) {}
    public record Task(@NotBlank @Size(max=120) String title, @Size(max=2000) String description,
            @NotNull LocalDate dueDate, @NotBlank @Pattern(regexp="(?:[01][0-9]|2[0-3]):[0-5][0-9]") String dueTime,
            @NotBlank @Pattern(regexp="普通|重要|紧急") String priority, String goalId) {}
    public record Event(@NotBlank @Size(max=120) String title, @NotBlank String categoryId,
            @Size(max=4000) String description, @NotNull LocalDate date,
            @NotBlank @Pattern(regexp="普通|重要|重大") String importance, String sourceGoalId) {}
    public record Category(@NotBlank @Size(max=24) String name) {}
    public record FinanceRecord(@NotBlank @Pattern(regexp="INCOME|EXPENSE") String type,
            @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal amount,
            @NotBlank @Size(max=40) String category, @NotNull LocalDate date,
            @Size(max=500) String note) {}
    public record Assets(@NotNull @DecimalMin("0") @Digits(integer=10,fraction=2) BigDecimal cash,
            @NotNull @DecimalMin("0") @Digits(integer=10,fraction=2) BigDecimal other,
            @NotNull @DecimalMin("0") @Digits(integer=10,fraction=2) BigDecimal debt) {}
    public record Profile(@NotBlank @Size(max=40) String nickname, @Size(max=500) String bio,
            @Size(max=600000) String avatar) {}
    public record Status(@NotNull Boolean completed) {}
}
