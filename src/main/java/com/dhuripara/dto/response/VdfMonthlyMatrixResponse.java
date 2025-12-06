package com.dhuripara.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class VdfMonthlyMatrixResponse {
    private Integer year;
    private List<FamilyRow> families;

    @Data
    public static class FamilyRow {
        private UUID familyId;
        private String familyHeadName;
        private String memberName;
        private BigDecimal monthlyAmount;
        private Map<String, MonthStatus> months; // month1 to month12
        private BigDecimal totalPaid;
        private BigDecimal totalDue;
        private Integer paidMonths;
        private Integer pendingMonths;
    }

    @Data
    public static class MonthStatus {
        private Boolean paid;
        private BigDecimal amount;
        private LocalDate paymentDate;
    }
}