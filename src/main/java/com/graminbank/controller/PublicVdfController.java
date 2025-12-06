package com.graminbank.controller;

import com.graminbank.dto.response.*;
import com.graminbank.service.VdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/vdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicVdfController {

    private final VdfService vdfService;

    @GetMapping("/summary")
    public ResponseEntity<VdfSummaryResponse> getSummary() {
        VdfSummaryResponse summary = vdfService.getSummary();
        return ResponseEntity.ok(summary);
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

    @GetMapping("/contributions/monthly-matrix")
    public ResponseEntity<Map<String, Object>> getMonthlyContributionMatrix(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.Year.now().getValue();
        }
        Map<String, Object> matrix = vdfService.getMonthlyContributionMatrix(year);
        return ResponseEntity.ok(matrix);
    }
}