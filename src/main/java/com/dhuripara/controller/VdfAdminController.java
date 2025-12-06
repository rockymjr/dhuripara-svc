package com.dhuripara.controller;

import com.dhuripara.dto.request.*;
import com.dhuripara.dto.response.*;
import com.dhuripara.model.*;
import com.dhuripara.service.VdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/vdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class VdfAdminController {

    private final VdfService vdfService;

    // ============================================
    // FAMILY CONFIG ENDPOINTS
    // ============================================

    @PostMapping("/families")
    public ResponseEntity<VdfFamilyConfig> registerFamily(@Valid @RequestBody VdfFamilyConfigRequest request) {
        VdfFamilyConfig family = vdfService.registerFamily(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(family);
    }

    @PutMapping("/families/{familyId}")
    public ResponseEntity<VdfFamilyConfig> updateFamily(
            @PathVariable UUID familyId,
            @Valid @RequestBody VdfFamilyConfigRequest request) {
        VdfFamilyConfig family = vdfService.updateFamily(familyId, request);
        return ResponseEntity.ok(family);
    }

    @GetMapping("/families")
    public ResponseEntity<List<VdfFamilyConfig>> getAllFamilies() {
        List<VdfFamilyConfig> families = vdfService.getAllActiveFamilies();
        return ResponseEntity.ok(families);
    }

    // ============================================
    // MONTHLY CONFIG ENDPOINTS
    // ============================================

    @PostMapping("/monthly-config")
    public ResponseEntity<VdfMonthlyConfig> createOrUpdateMonthlyConfig(
            @Valid @RequestBody VdfMonthlyConfigRequest request) {
        VdfMonthlyConfig config = vdfService.createOrUpdateMonthlyConfig(request);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/monthly-config")
    public ResponseEntity<List<VdfMonthlyConfig>> getAllMonthlyConfigs() {
        List<VdfMonthlyConfig> configs = vdfService.getAllMonthlyConfigs();
        return ResponseEntity.ok(configs);
    }

    // ============================================
    // EXEMPTION ENDPOINTS
    // ============================================

    @PostMapping("/exemptions")
    public ResponseEntity<VdfFamilyExemption> addExemption(
            @Valid @RequestBody VdfExemptionRequest request) {
        // TODO: Get current admin user ID from security context
        VdfFamilyExemption exemption = vdfService.addExemption(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(exemption);
    }

    @DeleteMapping("/exemptions/{exemptionId}")
    public ResponseEntity<Void> removeExemption(@PathVariable UUID exemptionId) {
        vdfService.removeExemption(exemptionId);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // CONTRIBUTION ENDPOINTS
    // ============================================

    @PostMapping("/contributions")
    public ResponseEntity<VdfContribution> recordContribution(
            @Valid @RequestBody VdfContributionRequest request) {
        VdfContribution contribution = vdfService.recordContribution(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(contribution);
    }

    @GetMapping("/contributions/family/{familyId}")
    public ResponseEntity<List<VdfContribution>> getContributionsByFamily(
            @PathVariable UUID familyId) {
        List<VdfContribution> contributions = vdfService.getContributionsByFamily(familyId);
        return ResponseEntity.ok(contributions);
    }

    // ============================================
    // EXPENSE ENDPOINTS
    // ============================================

    @PostMapping("/expenses")
    public ResponseEntity<VdfExpense> recordExpense(@Valid @RequestBody VdfExpenseRequest request) {
        VdfExpense expense = vdfService.recordExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    @GetMapping("/expenses")
    public ResponseEntity<List<VdfExpense>> getAllExpenses() {
        List<VdfExpense> expenses = vdfService.getAllExpenses();
        return ResponseEntity.ok(expenses);
    }

    // ============================================
    // REPORTING ENDPOINTS
    // ============================================

    @GetMapping("/balance")
    public ResponseEntity<VdfBalanceSummaryResponse> getBalanceSummary() {
        VdfBalanceSummaryResponse summary = vdfService.getBalanceSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/reports/monthly/{monthYear}")
    public ResponseEntity<List<VdfFamilyMonthlySummaryResponse>> getMonthlyReport(
            @PathVariable String monthYear) {
        List<VdfFamilyMonthlySummaryResponse> report = vdfService.getMonthlyReport(monthYear);
        return ResponseEntity.ok(report);
    }
}