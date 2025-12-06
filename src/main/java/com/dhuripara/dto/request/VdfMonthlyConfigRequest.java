package com.dhuripara.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VdfMonthlyConfigRequest {
    @NotBlank(message = "Month year is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Month year must be in format YYYY-MM")
    private String monthYear;

    @NotNull(message = "Required amount is required")
    @DecimalMin(value = "0", message = "Required amount must be >= 0")
    private BigDecimal requiredAmount;

    private String description;
}