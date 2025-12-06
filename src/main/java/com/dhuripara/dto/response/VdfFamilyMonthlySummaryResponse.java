package com.dhuripara.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class VdfFamilyMonthlySummaryResponse {
    private UUID familyId;
    private UUID memberId;
    private String memberName;
    private String phone;
    private String monthYear;
    private BigDecimal requiredAmount;
    private Boolean isExempted;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
}



