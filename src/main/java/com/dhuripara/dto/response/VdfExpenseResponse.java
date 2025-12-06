package com.dhuripara.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfExpenseResponse {
    private UUID id;
    private LocalDate expenseDate;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String description;
    private String vendorName;
    private String billNumber;
    private String paymentMethod;
    private String approvedBy;
    private String notes;
    private String category;
}