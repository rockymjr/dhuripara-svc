package com.dhuripara.controller;

import com.dhuripara.dto.request.*;
import com.dhuripara.dto.response.*;
import com.dhuripara.model.VdfFamilyConfig;
import com.dhuripara.service.VdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class VdfAdminController {

    private final VdfService vdfService;

    // ==================== DEPOSITS ====================

    @PostMapping("/deposits")
    public ResponseEntity<VdfDepositResponse> createDeposit(@Valid @RequestBody VdfDepositRequest request) {
        VdfDepositResponse response = vdfService.createDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/deposits")
    public ResponseEntity<List<VdfDepositResponse>> getDeposits(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        List<VdfDepositResponse> deposits = vdfService.getDepositsByYear(year);
        return ResponseEntity.ok(deposits);
    }

    // ==================== EXPENSES ====================

    @PostMapping("/expenses")
    public ResponseEntity<VdfExpenseResponse> createExpense(@Valid @RequestBody VdfExpenseRequest request) {
        VdfExpenseResponse response = vdfService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/expenses")
    public ResponseEntity<List<VdfExpenseResponse>> getExpenses(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        List<VdfExpenseResponse> expenses = vdfService.getExpensesByYear(year);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/category-summary")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryExpenses(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        Map<String, BigDecimal> summary = vdfService.getCategoryExpenses(year);
        return ResponseEntity.ok(summary);
    }

    // ==================== FAMILY CONFIGURATION ====================

    @PostMapping("/families")
    public ResponseEntity<VdfFamilyConfig> createOrUpdateFamily(
            @Valid @RequestBody VdfFamilyConfigRequest request) {
        VdfFamilyConfig config = vdfService.createOrUpdateFamilyConfig(request);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/families")
    public ResponseEntity<List<VdfFamilyConfig>> getActiveFamilies() {
        List<VdfFamilyConfig> families = vdfService.getAllActiveFamilies();
        return ResponseEntity.ok(families);
    }

    // ==================== CONTRIBUTIONS ====================

    @PostMapping("/contributions")
    public ResponseEntity<Void> recordContribution(@Valid @RequestBody VdfContributionRequest request) {
        vdfService.recordContribution(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/contributions/monthly-matrix")
    public ResponseEntity<List<VdfFamilyMonthlySummaryResponse>> getMonthlyMatrix(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        List<VdfFamilyMonthlySummaryResponse> matrix = vdfService.getMonthlyContributionMatrix(year);
        return ResponseEntity.ok(matrix);
    }

    // ==================== REPORTS ====================

    @GetMapping("/summary")
    public ResponseEntity<VdfSummaryResponse> getSummary() {
        VdfSummaryResponse summary = vdfService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<List<VdfMonthlyReportResponse>> getMonthlyReport(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        List<VdfMonthlyReportResponse> report = vdfService.getMonthlyReport(year);
        return ResponseEntity.ok(report);
    }
}