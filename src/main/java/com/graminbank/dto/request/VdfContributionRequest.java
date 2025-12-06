package com.graminbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfContributionRequest {

    @NotNull(message = "Family config ID is required")
    private UUID familyConfigId;

    @NotNull(message = "Payment month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer paymentMonth;

    @NotNull(message = "Payment year is required")
    @Min(value = 2024, message = "Year must be 2024 or later")
    private Integer paymentYear;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amountPaid;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private String paymentMethod;
    private String receiptNumber;
    private String collectedBy;
    private String notes;
}