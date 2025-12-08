package com.dhuripara.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Data
public class VdfFamilyMonthlySummaryResponse {
    private UUID familyConfigId;
    private String familyHeadName;
    private String memberPhone;
    private BigDecimal monthlyAmount;

    // Month-wise payment status (1-12)
    private Boolean[] paidMonths = new Boolean[12]; // Jan to Dec
    // Month-wise exemption flags
    private Boolean[] exemptedMonths = new Boolean[12];

    // Map of month-year to exemption flag (e.g., {"2023-07": true})
    private Map<String, Boolean> exemptionsMap = new HashMap<>();

    private Integer totalPaidMonths;
    private Integer totalPendingMonths;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
    // All-time totals (since effectiveFrom till today)
    private BigDecimal totalPaidAllTime;
    private BigDecimal totalDueAllTime;
    // Alias fields kept for frontend compatibility
    private BigDecimal totalAmountPaid;
    private BigDecimal totalAmountDue;
}



