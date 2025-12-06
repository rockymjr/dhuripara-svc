package com.dhuripara.controller;

import com.dhuripara.dto.response.VdfExpenseResponse;
import com.dhuripara.dto.response.VdfSummaryResponse;
import com.dhuripara.model.VdfExpense;
import com.dhuripara.service.VdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/vdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VdfPublicController {

    private final VdfService vdfService;

    @GetMapping("/summary")
    public ResponseEntity<VdfSummaryResponse> getSummary() {
        VdfSummaryResponse summary = vdfService.getPublicSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/expenses")
    public ResponseEntity<Page<VdfExpenseResponse>> getExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<VdfExpense> expenses = vdfService.getAllExpenses();

        // Convert to response DTOs
        List<VdfExpenseResponse> expenseResponses = expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, expenseResponses.size());
        List<VdfExpenseResponse> pageContent = expenseResponses.subList(start, end);

        // Create Page manually (simplified - you might want to use PageImpl)
        Page<VdfExpenseResponse> expensePage = new org.springframework.data.domain.PageImpl<>(
                pageContent,
                PageRequest.of(page, size),
                expenseResponses.size()
        );

        return ResponseEntity.ok(expensePage);
    }

    @GetMapping("/contributions/monthly-matrix")
    public ResponseEntity<?> getMonthlyMatrix(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        // TODO: Implement monthly matrix logic
        return ResponseEntity.ok(java.util.Map.of(
                "year", year,
                "families", java.util.List.of(),
                "message", "Feature coming soon"
        ));
    }

    private VdfExpenseResponse convertToResponse(VdfExpense expense) {
        VdfExpenseResponse response = new VdfExpenseResponse();
        response.setId(expense.getId());
        response.setExpenseDate(expense.getExpenseDate());
        response.setAmount(expense.getAmount());
        response.setCategory(expense.getCategory());
        response.setCategoryName(expense.getCategory());
        response.setDescription(expense.getDescription());
        response.setNotes(expense.getNotes());
        return response;
    }
}