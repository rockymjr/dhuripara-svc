package com.graminbank.controller;

import com.graminbank.dto.request.*;
import com.graminbank.dto.response.*;
import com.graminbank.model.VdfExpenseCategory;
import com.graminbank.service.VdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/admin/vdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class AdminVdfController {

    private final VdfService vdfService;

    // ==================== Family Configuration ====================

    @PostMapping("/families")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VdfFamilyConfigResponse> createFamilyConfig(
            @Valid @RequestBody VdfFamilyConfigRequest request) {
        VdfFamilyConfigResponse response = vdfService.createFamilyConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/families/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VdfFamilyConfigResponse> updateFamilyConfig(
            @PathVariable UUID id,
            @Valid @RequestBody VdfFamilyConfigRequest request) {
        VdfFamilyConfigResponse response = vdfService.updateFamilyConfig(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/families")
    public ResponseEntity<List<VdfFamilyConfigResponse>> getAllFamilies(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        List<VdfFamilyConfigResponse> families = activeOnly
                ? vdfService.getActiveFamilies()
                : vdfService.getAllFamilies();
        return ResponseEntity.ok(families);
    }

    // ==================== Contributions ====================

    @PostMapping("/contributions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VdfContributionResponse> recordContribution(
            @Valid @RequestBody VdfContributionRequest request) {
        VdfContributionResponse response = vdfService.recordContribution(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/contributions/family/{familyConfigId}")
    public ResponseEntity<List<VdfContributionResponse>> getFamilyContributions(
            @PathVariable UUID familyConfigId,
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.Year.now().getValue();
        }
        List<VdfContributionResponse> contributions =
                vdfService.getFamilyContributions(familyConfigId, year);
        return ResponseEntity.ok(contributions);
    }

    @GetMapping("/contributions/monthly-matrix")
    public ResponseEntity<Map<String, Object>> getMonthlyContributionMatrix(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.Year.now().getValue();
        }
        Map<String, Object> matrix = vdfService.getMonthlyContributionMatrix(year);
        return ResponseEntity.ok(matrix);
    }

    // ==================== Expenses ====================

    @PostMapping("/expenses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VdfExpenseResponse> createExpense(
            @Valid @RequestBody VdfExpenseRequest request) {
        VdfExpenseResponse response = vdfService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/expenses")
    public ResponseEntity<Page<VdfExpenseResponse>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<VdfExpenseResponse> expenses = vdfService.getAllExpenses(
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/category/{categoryId}")
    public ResponseEntity<List<VdfExpenseResponse>> getExpensesByCategory(
            @PathVariable UUID categoryId) {
        List<VdfExpenseResponse> expenses = vdfService.getExpensesByCategory(categoryId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expense-categories")
    public ResponseEntity<List<VdfExpenseCategory>> getAllCategories() {
        List<VdfExpenseCategory> categories = vdfService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // ==================== Reports & Summary ====================

    @GetMapping("/summary")
    public ResponseEntity<VdfSummaryResponse> getSummary() {
        VdfSummaryResponse summary = vdfService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<List<VdfMonthlyReportResponse>> getMonthlyReport(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.Year.now().getValue();
        }
        List<VdfMonthlyReportResponse> report = vdfService.getMonthlyReport(year);
        return ResponseEntity.ok(report);
    }
}