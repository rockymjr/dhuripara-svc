package com.graminbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfExpenseRequest {

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;

    private String vendorName;
    private String billNumber;
    private String paymentMethod;
    private String approvedBy;
    private String notes;
}