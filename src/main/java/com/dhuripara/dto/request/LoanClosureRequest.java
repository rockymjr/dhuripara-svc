package com.dhuripara.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanClosureRequest {

    @NotNull(message = "Return date is required")
    private LocalDate returnDate;

    @DecimalMin(value = "0", message = "Discount cannot be negative")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    private BigDecimal paymentAmount;
}