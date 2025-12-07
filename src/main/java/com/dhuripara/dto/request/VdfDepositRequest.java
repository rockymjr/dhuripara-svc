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

    @NotBlank(message = "Source type is required")
    @Pattern(regexp = "VILLAGER|DONATION|OTHER", message = "Invalid source type")
    private String sourceType;

    @NotBlank(message = "Source name is required")
    private String sourceName;

    @NotNull(message = "Deposit category is required")
    private UUID categoryId; // New field for category

    private UUID memberId; // Optional: if sourceType is VILLAGER

    private String notes;
}


