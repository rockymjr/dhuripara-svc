package com.graminbank.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfContributionResponse {
    private UUID id;
    private UUID familyConfigId;
    private String familyHeadName;
    private Integer paymentMonth;
    private String monthName;
    private Integer paymentYear;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String receiptNumber;
    private String collectedBy;
    private String notes;
}