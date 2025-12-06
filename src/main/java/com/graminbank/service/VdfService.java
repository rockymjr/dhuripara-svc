package com.graminbank.service;

import com.graminbank.dto.request.*;
import com.graminbank.dto.response.*;
import com.graminbank.exception.BusinessException;
import com.graminbank.exception.ResourceNotFoundException;
import com.graminbank.model.*;
import com.graminbank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VdfService {

    private final VdfFamilyConfigRepository familyConfigRepository;
    private final VdfContributionRepository contributionRepository;
    private final VdfExpenseRepository expenseRepository;
    private final VdfExpenseCategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    // ==================== Family Configuration ====================

    @Transactional
    public VdfFamilyConfigResponse createFamilyConfig(VdfFamilyConfigRequest request) {
        log.info("Creating VDF family config for member: {}", request.getMemberId());

        // Check if config already exists
        if (familyConfigRepository.findByMemberId(request.getMemberId()).isPresent()) {
            throw new BusinessException("VDF configuration already exists for this member");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        VdfFamilyConfig config = new VdfFamilyConfig();
        config.setMember(member);
        config.setFamilyHeadName(request.getFamilyHeadName());
        config.setIsContributionEnabled(request.getIsContributionEnabled());
        config.setEffectiveFrom(request.getEffectiveFrom());
        config.setMonthlyAmount(request.getMonthlyAmount());
        config.setNotes(request.getNotes());

        VdfFamilyConfig saved = familyConfigRepository.save(config);
        return convertToFamilyConfigResponse(saved);
    }

    @Transactional
    public VdfFamilyConfigResponse updateFamilyConfig(UUID id, VdfFamilyConfigRequest request) {
        VdfFamilyConfig config = familyConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Family config not found"));

        config.setFamilyHeadName(request.getFamilyHeadName());
        config.setIsContributionEnabled(request.getIsContributionEnabled());
        config.setEffectiveFrom(request.getEffectiveFrom());
        config.setMonthlyAmount(request.getMonthlyAmount());
        config.setNotes(request.getNotes());

        VdfFamilyConfig updated = familyConfigRepository.save(config);
        return convertToFamilyConfigResponse(updated);
    }

    public List<VdfFamilyConfigResponse> getAllFamilies() {
        return familyConfigRepository.findAll().stream()
                .map(this::convertToFamilyConfigResponse)
                .collect(Collectors.toList());
    }

    public List<VdfFamilyConfigResponse> getActiveFamilies() {
        return familyConfigRepository.findByIsContributionEnabledTrue().stream()
                .map(this::convertToFamilyConfigResponse)
                .collect(Collectors.toList());
    }

    // ==================== Contributions ====================

    @Transactional
    public VdfContributionResponse recordContribution(VdfContributionRequest request) {
        log.info("Recording VDF contribution for family: {}, month: {}, year: {}",
                request.getFamilyConfigId(), request.getPaymentMonth(), request.getPaymentYear());

        VdfFamilyConfig config = familyConfigRepository.findById(request.getFamilyConfigId())
                .orElseThrow(() -> new ResourceNotFoundException("Family config not found"));

        if (!config.getIsContributionEnabled()) {
            throw new BusinessException("Contribution is not enabled for this family");
        }

        // Check if already paid for this month
        Optional<VdfContribution> existing = contributionRepository
                .findByFamilyConfigIdAndPaymentMonthAndPaymentYear(
                        request.getFamilyConfigId(),
                        request.getPaymentMonth(),
                        request.getPaymentYear()
                );

        if (existing.isPresent()) {
            throw new BusinessException("Contribution already recorded for this month");
        }

        VdfContribution contribution = new VdfContribution();
        contribution.setFamilyConfig(config);
        contribution.setPaymentMonth(request.getPaymentMonth());
        contribution.setPaymentYear(request.getPaymentYear());
        contribution.setAmountPaid(request.getAmountPaid());
        contribution.setPaymentDate(request.getPaymentDate());
        contribution.setPaymentMethod(request.getPaymentMethod());
        contribution.setReceiptNumber(request.getReceiptNumber());
        contribution.setCollectedBy(request.getCollectedBy());
        contribution.setNotes(request.getNotes());

        VdfContribution saved = contributionRepository.save(contribution);
        return convertToContributionResponse(saved);
    }

    public List<VdfContributionResponse> getFamilyContributions(UUID familyConfigId, Integer year) {
        return contributionRepository.findByFamilyConfigIdAndPaymentYear(familyConfigId, year)
                .stream()
                .map(this::convertToContributionResponse)
                .collect(Collectors.toList());
    }

    // ==================== Expenses ====================

    @Transactional
    public VdfExpenseResponse createExpense(VdfExpenseRequest request) {
        log.info("Creating VDF expense: {}", request.getDescription());

        VdfExpenseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        VdfExpense expense = new VdfExpense();
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setVendorName(request.getVendorName());
        expense.setBillNumber(request.getBillNumber());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setApprovedBy(request.getApprovedBy());
        expense.setNotes(request.getNotes());

        VdfExpense saved = expenseRepository.save(expense);
        return convertToExpenseResponse(saved);
    }

    public Page<VdfExpenseResponse> getAllExpenses(Pageable pageable) {
        return expenseRepository.findAllByOrderByExpenseDateDesc(pageable)
                .map(this::convertToExpenseResponse);
    }

    public List<VdfExpenseResponse> getExpensesByCategory(UUID categoryId) {
        return expenseRepository.findByCategoryIdOrderByExpenseDateDesc(categoryId)
                .stream()
                .map(this::convertToExpenseResponse)
                .collect(Collectors.toList());
    }

    // ==================== Reports & Summary ====================

    public VdfSummaryResponse getSummary() {
        VdfSummaryResponse response = new VdfSummaryResponse();

        Long totalFamilies = familyConfigRepository.count();
        Long activeFamilies = familyConfigRepository.countByIsContributionEnabledTrue();
        BigDecimal totalCollected = contributionRepository.getTotalCollectedAllTime();
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesAllTime();

        response.setTotalFamilies(totalFamilies.intValue());
        response.setActiveFamilies(activeFamilies.intValue());
        response.setTotalCollected(totalCollected);
        response.setTotalExpenses(totalExpenses);
        response.setCurrentBalance(totalCollected.subtract(totalExpenses));
        response.setCurrentYear(String.valueOf(Year.now().getValue()));

        return response;
    }

    public List<VdfMonthlyReportResponse> getMonthlyReport(Integer year) {
        List<VdfMonthlyReportResponse> reports = new ArrayList<>();
        Long activeFamilies = familyConfigRepository.countByIsContributionEnabledTrue();

        for (int month = 1; month <= 12; month++) {
            VdfMonthlyReportResponse report = new VdfMonthlyReportResponse();
            report.setYear(year);
            report.setMonth(month);
            report.setMonthName(Month.of(month).name());

            List<VdfContribution> contributions = contributionRepository
                    .findByPaymentMonthAndPaymentYear(month, year);

            report.setFamiliesPaid(contributions.size());
            report.setFamiliesPending(activeFamilies.intValue() - contributions.size());

            BigDecimal collected = contributionRepository.getTotalCollectedForMonth(month, year);
            BigDecimal expenses = expenseRepository.getTotalExpensesForMonth(month, year);

            report.setTotalCollected(collected);
            report.setTotalExpenses(expenses);

            reports.add(report);
        }

        return reports;
    }

    public Map<String, Object> getMonthlyContributionMatrix(Integer year) {
        List<VdfFamilyConfig> families = familyConfigRepository.findByIsContributionEnabledTrue();
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> familyData = new ArrayList<>();

        for (VdfFamilyConfig family : families) {
            Map<String, Object> familyRow = new HashMap<>();
            familyRow.put("familyId", family.getId());
            familyRow.put("familyHeadName", family.getFamilyHeadName());
            familyRow.put("memberName", family.getMember().getFirstName() + " " +
                    family.getMember().getLastName());

            List<VdfContribution> yearContributions = contributionRepository
                    .findByFamilyConfigIdAndPaymentYear(family.getId(), year);

            Map<Integer, VdfContribution> monthMap = yearContributions.stream()
                    .collect(Collectors.toMap(VdfContribution::getPaymentMonth, c -> c));

            BigDecimal totalPaid = BigDecimal.ZERO;
            int paidMonths = 0;

            Map<String, Object> months = new HashMap<>();
            for (int month = 1; month <= 12; month++) {
                if (monthMap.containsKey(month)) {
                    VdfContribution contrib = monthMap.get(month);
                    months.put("month" + month, Map.of(
                            "paid", true,
                            "amount", contrib.getAmountPaid(),
                            "date", contrib.getPaymentDate()
                    ));
                    totalPaid = totalPaid.add(contrib.getAmountPaid());
                    paidMonths++;
                } else {
                    months.put("month" + month, Map.of("paid", false));
                }
            }

            familyRow.put("months", months);
            familyRow.put("totalPaid", totalPaid);
            familyRow.put("paidMonths", paidMonths);
            familyRow.put("pendingMonths", 12 - paidMonths);
            familyRow.put("totalDue", family.getMonthlyAmount()
                    .multiply(BigDecimal.valueOf(12 - paidMonths)));

            familyData.add(familyRow);
        }

        result.put("year", year);
        result.put("families", familyData);

        return result;
    }

    // ==================== Helper Methods ====================

    private VdfFamilyConfigResponse convertToFamilyConfigResponse(VdfFamilyConfig config) {
        VdfFamilyConfigResponse response = new VdfFamilyConfigResponse();
        response.setId(config.getId());
        response.setMemberId(config.getMember().getId());
        response.setMemberName(config.getMember().getFirstName() + " " +
                config.getMember().getLastName());
        response.setFamilyHeadName(config.getFamilyHeadName());
        response.setIsContributionEnabled(config.getIsContributionEnabled());
        response.setEffectiveFrom(config.getEffectiveFrom());
        response.setMonthlyAmount(config.getMonthlyAmount());
        response.setNotes(config.getNotes());

        // Calculate paid and pending
        int currentYear = Year.now().getValue();
        List<VdfContribution> yearContributions = contributionRepository
                .findByFamilyConfigIdAndPaymentYear(config.getId(), currentYear);

        response.setTotalPaidMonths(yearContributions.size());
        response.setTotalPendingMonths(12 - yearContributions.size());

        BigDecimal totalPaid = yearContributions.stream()
                .map(VdfContribution::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setTotalAmountPaid(totalPaid);
        response.setTotalAmountDue(config.getMonthlyAmount()
                .multiply(BigDecimal.valueOf(12 - yearContributions.size())));

        return response;
    }

    private VdfContributionResponse convertToContributionResponse(VdfContribution contribution) {
        VdfContributionResponse response = new VdfContributionResponse();
        response.setId(contribution.getId());
        response.setFamilyConfigId(contribution.getFamilyConfig().getId());
        response.setFamilyHeadName(contribution.getFamilyConfig().getFamilyHeadName());
        response.setPaymentMonth(contribution.getPaymentMonth());
        response.setMonthName(Month.of(contribution.getPaymentMonth()).name());
        response.setPaymentYear(contribution.getPaymentYear());
        response.setAmountPaid(contribution.getAmountPaid());
        response.setPaymentDate(contribution.getPaymentDate());
        response.setPaymentMethod(contribution.getPaymentMethod());
        response.setReceiptNumber(contribution.getReceiptNumber());
        response.setCollectedBy(contribution.getCollectedBy());
        response.setNotes(contribution.getNotes());
        return response;
    }

    private VdfExpenseResponse convertToExpenseResponse(VdfExpense expense) {
        VdfExpenseResponse response = new VdfExpenseResponse();
        response.setId(expense.getId());
        response.setExpenseDate(expense.getExpenseDate());
        response.setCategoryId(expense.getCategory().getId());
        response.setCategoryName(expense.getCategory().getCategoryName());
        response.setAmount(expense.getAmount());
        response.setDescription(expense.getDescription());
        response.setVendorName(expense.getVendorName());
        response.setBillNumber(expense.getBillNumber());
        response.setPaymentMethod(expense.getPaymentMethod());
        response.setApprovedBy(expense.getApprovedBy());
        response.setNotes(expense.getNotes());
        return response;
    }

    public List<VdfExpenseCategory> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByCategoryName();
    }
}