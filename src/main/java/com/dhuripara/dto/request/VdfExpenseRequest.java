package com.dhuripara.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfExpenseRequest {
    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotBlank(message = "Description is required")
    private String description;

    private String descriptionBn;

    private String notes;
}