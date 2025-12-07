package com.dhuripara.dto.response;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class VdfContributionResponse {
    private UUID id;
    private UUID familyId;
    private String memberName;
    private Integer month;  // 1-12
    private Integer year;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private List<MonthAllocationResponse> monthAllocations;
    private String notes;

    @Data
    public static class MonthAllocationResponse {
        private String month;
        private BigDecimal amount;
    }
}