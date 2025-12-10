package com.dhuripara.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfDepositRequest {
    @NotNull(message = "Deposit date is required")
    private LocalDate depositDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String sourceName;

    @NotNull(message = "Deposit category is required")
    private UUID categoryId; // New field for category

    private UUID memberId; // Optional: if deposit is linked to a member

    private String notes;
}


