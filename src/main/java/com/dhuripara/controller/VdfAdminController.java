package com.dhuripara.controller;

import com.dhuripara.dto.request.VdfContributionRequest;
import com.dhuripara.dto.request.VdfDepositRequest;
import com.dhuripara.dto.request.VdfExpenseRequest;
import com.dhuripara.dto.request.VdfFamilyConfigRequest;
import com.dhuripara.dto.response.*;
import com.dhuripara.model.VdfExpenseCategory;
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
import java.util.UUID;

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

    @DeleteMapping("/deposits/{id}")
    public ResponseEntity<Void> deleteDeposit(@PathVariable UUID id) {
        vdfService.deleteDeposit(id);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<VdfFamilyConfigResponse> createFamily(
            @Valid @RequestBody VdfFamilyConfigRequest request) {
        VdfFamilyConfig config = vdfService.createOrUpdateFamilyConfig(request);
        VdfFamilyConfigResponse response = vdfService.getAllFamilies(false).stream()
            .filter(f -> f.getId().equals(config.getId()))
            .findFirst()
            .orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/families/{id}")
    public ResponseEntity<VdfFamilyConfigResponse> updateFamily(
            @PathVariable UUID id,
            @Valid @RequestBody VdfFamilyConfigRequest request) {
        VdfFamilyConfigResponse response = vdfService.updateFamilyConfig(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/families")
    public ResponseEntity<List<VdfFamilyConfigResponse>> getAllFamilies(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        List<VdfFamilyConfigResponse> families = vdfService.getAllFamilies(activeOnly);
        return ResponseEntity.ok(families);
    }

    @PostMapping("/family-exemptions")
    public ResponseEntity<Void> createFamilyExemption(@Valid @RequestBody com.dhuripara.dto.request.VdfExemptionRequest request) {
        vdfService.createExemption(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/family-exemptions")
    public ResponseEntity<Void> deleteFamilyExemption(@RequestParam java.util.UUID familyId, @RequestParam String monthYear) {
        vdfService.deleteExemption(familyId, monthYear);
        return ResponseEntity.ok().build();
    }

    // ==================== CONTRIBUTIONS ====================

    @PostMapping("/contributions")
    public ResponseEntity<Void> recordContribution(@Valid @RequestBody VdfContributionRequest request) {
        vdfService.recordContribution(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @PostMapping("/contributions/bulk")
    public ResponseEntity<?> recordBulkContributions(@Valid @RequestBody com.dhuripara.dto.request.VdfBulkContributionRequest request) {
        try {
            vdfService.recordBulkContributions(request);
            return ResponseEntity.ok(java.util.Map.of("message", "Contributions recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/contributions/family/{familyConfigId}")
    public ResponseEntity<List<VdfContributionResponse>> getFamilyContributions(
            @PathVariable UUID familyConfigId,
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        List<VdfContributionResponse> contributions = vdfService.getFamilyContributions(familyConfigId, year);
        return ResponseEntity.ok(contributions);
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

    @GetMapping("/expense-categories")
    public ResponseEntity<List<VdfExpenseCategory>> getExpenseCategories() {
        return ResponseEntity.ok(vdfService.getExpenseCategories());
    }

    @GetMapping("/deposit-categories")
    public ResponseEntity<List<VdfDepositCategoryResponse>> getDepositCategories() {
        return ResponseEntity.ok(vdfService.getDepositCategories());
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<VdfExpenseResponse> updateExpense(
            @PathVariable UUID id,
            @Valid @RequestBody VdfExpenseRequest request) {
        return ResponseEntity.ok(vdfService.updateExpense(id, request));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID id) {
        vdfService.deleteExpense(id);
        return ResponseEntity.ok().build();
    }


//    @GetMapping("/expenses/category/{categoryId}")
//    public ResponseEntity<Page<VdfExpenseResponse>> getExpensesByCategory(
//            @PathVariable UUID categoryId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size) {
//        return ResponseEntity.ok(vdfService.getExpensesByCategory(categoryId, page, size));
//    }
}