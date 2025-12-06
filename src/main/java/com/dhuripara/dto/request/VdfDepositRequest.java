package com.dhuripara.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VdfDepositRequest {
    @NotNull(message = "Deposit date is required")
    private LocalDate depositDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Source type is required")
    private String sourceType; // VILLAGER, DONATION, GRANT, OTHER

    private String sourceName;

    @NotBlank(message = "Description is required")
    private String description;

    private String notes;
}
