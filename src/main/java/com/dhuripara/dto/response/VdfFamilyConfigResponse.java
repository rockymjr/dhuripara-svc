package com.dhuripara.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfFamilyConfigResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private String familyHeadName;
    private Boolean isContributionEnabled;
    private LocalDate effectiveFrom;
    private BigDecimal monthlyAmount;
    private String notes;

    // Additional computed fields
    private Integer totalPaidMonths;
    private Integer totalPendingMonths;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalAmountDue;
}