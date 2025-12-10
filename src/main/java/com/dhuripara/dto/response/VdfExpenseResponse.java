package com.dhuripara.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfExpenseResponse {
    private UUID id;
    private LocalDate expenseDate;
    private BigDecimal amount;
    private UUID categoryId;
    private String categoryName;
    private String categoryNameBn;
    private String description;
    private String descriptionBn;
    private String notes;
}