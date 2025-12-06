package com.dhuripara.controller;


import com.dhuripara.dto.response.MemberStatementResponse;
import com.dhuripara.dto.response.YearlySettlementResponse;
import com.dhuripara.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/members/{memberId}/statement")
    public ResponseEntity<MemberStatementResponse> getMemberStatement(
            @PathVariable UUID memberId,
            @RequestParam(required = false) String year) {
        MemberStatementResponse response = reportService.getMemberStatement(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yearly-settlement")
    public ResponseEntity<YearlySettlementResponse> getYearlySettlement(
            @RequestParam(required = false) String year) {
        YearlySettlementResponse response = reportService.getYearlySettlement(year);
        return ResponseEntity.ok(response);
    }
}