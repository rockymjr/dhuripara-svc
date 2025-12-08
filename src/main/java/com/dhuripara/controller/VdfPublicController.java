package com.dhuripara.controller;

import com.dhuripara.dto.response.VdfDepositResponse;
import com.dhuripara.dto.response.VdfExpenseResponse;
import com.dhuripara.dto.response.VdfFamilyMonthlySummaryResponse;
import com.dhuripara.dto.response.VdfFamilyConfigResponse;
import com.dhuripara.dto.response.VdfSummaryResponse;
import com.dhuripara.dto.response.VdfDepositCategoryResponse;
import com.dhuripara.model.VdfExpenseCategory;
import com.dhuripara.service.VdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/public/vdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
class VdfPublicController {

    private final VdfService vdfService;

    @GetMapping("/summary")
    public ResponseEntity<VdfSummaryResponse> getSummary() {
        VdfSummaryResponse summary = vdfService.getSummary();
        return ResponseEntity.ok(summary);
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

    @GetMapping("/expenses")
    public ResponseEntity<List<VdfExpenseResponse>> getExpenses(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        List<VdfExpenseResponse> expenses = vdfService.getExpensesByYear(year);
        return ResponseEntity.ok(expenses);
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

    @GetMapping("/deposit-categories")
    public ResponseEntity<List<VdfDepositCategoryResponse>> getDepositCategories() {
        return ResponseEntity.ok(vdfService.getDepositCategories());
    }

    @GetMapping("/expense-categories")
    public ResponseEntity<List<VdfExpenseCategory>> getExpenseCategories() {
        return ResponseEntity.ok(vdfService.getExpenseCategories());
    }

    @GetMapping("/families")
    public ResponseEntity<List<com.dhuripara.dto.response.VdfFamilyConfigResponse>> getFamilies(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        List<com.dhuripara.dto.response.VdfFamilyConfigResponse> families = vdfService.getAllFamilies(activeOnly);
        return ResponseEntity.ok(families);
    }
}