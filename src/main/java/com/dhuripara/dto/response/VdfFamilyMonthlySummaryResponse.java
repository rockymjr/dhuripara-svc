package com.dhuripara.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class VdfFamilyMonthlySummaryResponse {
    private UUID familyConfigId;
    private String familyHeadName;
    private String memberPhone;
    private BigDecimal monthlyAmount;

    // Month-wise payment status (1-12)
    private Boolean[] paidMonths = new Boolean[12]; // Jan to Dec

    private Integer totalPaidMonths;
    private Integer totalPendingMonths;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
}



