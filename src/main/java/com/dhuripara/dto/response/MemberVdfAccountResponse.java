package com.dhuripara.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MemberVdfAccountResponse {
    private BigDecimal totalPaidAllTime;
    private BigDecimal totalDueAllTime;
    private BigDecimal currentYearDue;
    private List<VdfContributionResponse> contributions;
}
