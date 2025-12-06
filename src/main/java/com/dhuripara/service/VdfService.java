package com.dhuripara.service;

import com.dhuripara.dto.request.*;
import com.dhuripara.dto.response.*;
import com.dhuripara.exception.BusinessException;
import com.dhuripara.exception.ResourceNotFoundException;
import com.dhuripara.model.*;
import com.dhuripara.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VdfService {

    private final VdfDepositRepository depositRepository;
    private final VdfExpenseRepository expenseRepository;
    private final VdfFamilyConfigRepository familyConfigRepository;
    private final VdfContributionRepository contributionRepository;
    private final MemberRepository memberRepository;

    // ==================== DEPOSITS ====================

    @Transactional
    public VdfDepositResponse createDeposit(VdfDepositRequest request) {
        log.info("Creating VDF deposit from: {}", request.getSourceName());

        VdfDeposit deposit = new VdfDeposit();
        deposit.setDepositDate(request.getDepositDate());
        deposit.setAmount(request.getAmount());
        deposit.setSourceType(request.getSourceType());
        deposit.setSourceName(request.getSourceName());
        deposit.setNotes(request.getNotes());

        if (request.getMemberId() != null) {
            Member member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
            deposit.setMember(member);
        }

        VdfDeposit saved = depositRepository.save(deposit);
        return convertDepositToResponse(saved);
    }

    public List<VdfDepositResponse> getDepositsByYear(Integer year) {
        return depositRepository.findByYearOrderByDepositDateDesc(year).stream()
                .map(this::convertDepositToResponse)
                .collect(Collectors.toList());
    }

    // ==================== EXPENSES ====================

    @Transactional
    public VdfExpenseResponse createExpense(VdfExpenseRequest request) {
        log.info("Creating VDF expense: {}", request.getDescription());

        VdfExpense expense = new VdfExpense();
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setNotes(request.getNotes());

        VdfExpense saved = expenseRepository.save(expense);
        return convertExpenseToResponse(saved);
    }

    public List<VdfExpenseResponse> getExpensesByYear(Integer year) {
        return expenseRepository.findByYearOrderByExpenseDateDesc(year).stream()
                .map(this::convertExpenseToResponse)
                .collect(Collectors.toList());
    }

    public Map<String, BigDecimal> getCategoryExpenses(Integer year) {
        List<Object[]> results = expenseRepository.getCategoryTotalsByYear(year);
        Map<String, BigDecimal> categoryExpenses = new HashMap<>();

        for (Object[] result : results) {
            categoryExpenses.put((String) result[0], (BigDecimal) result[1]);
        }

        return categoryExpenses;
    }

    // ==================== FAMILY CONFIGURATION ====================

    @Transactional
    public VdfFamilyConfig createOrUpdateFamilyConfig(VdfFamilyConfigRequest request) {
        log.info("Creating/updating family config for member: {}", request.getMemberId());

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        VdfFamilyConfig config = familyConfigRepository.findByMemberId(request.getMemberId())
                .orElse(new VdfFamilyConfig());

        config.setMember(member);
        config.setFamilyHeadName(request.getFamilyHeadName());
        config.setIsContributionEnabled(request.getIsContributionEnabled());
        config.setEffectiveFrom(request.getEffectiveFrom());
        config.setMonthlyAmount(request.getMonthlyAmount());
        config.setNotes(request.getNotes());

        return familyConfigRepository.save(config);
    }

    public List<VdfFamilyConfig> getAllActiveFamilies() {
        return familyConfigRepository.findByIsContributionEnabledTrue();
    }

    // ==================== CONTRIBUTIONS ====================

    @Transactional
    public void recordContribution(VdfContributionRequest request) {
        log.info("Recording contribution for family config: {}", request.getFamilyConfigId());

        VdfFamilyConfig familyConfig = familyConfigRepository.findById(request.getFamilyConfigId())
                .orElseThrow(() -> new ResourceNotFoundException("Family config not found"));

        if (!familyConfig.getIsContributionEnabled()) {
            throw new BusinessException("Contributions are not enabled for this family");
        }

        // Check if already paid
        Optional<VdfContribution> existing = contributionRepository
                .findByFamilyConfigIdAndYearAndMonth(request.getFamilyConfigId(),
                        request.getYear(), request.getMonth());

        if (existing.isPresent()) {
            throw new BusinessException("Contribution already recorded for this month");
        }

        VdfContribution contribution = new VdfContribution();
        contribution.setFamilyConfig(familyConfig);
        contribution.setYear(request.getYear());
        contribution.setMonth(request.getMonth());
        contribution.setAmount(request.getAmount());
        contribution.setPaymentDate(request.getPaymentDate());
        contribution.setNotes(request.getNotes());

        contributionRepository.save(contribution);
    }

    public List<VdfFamilyMonthlySummaryResponse> getMonthlyContributionMatrix(Integer year) {
        List<VdfFamilyConfig> activeFamilies = familyConfigRepository.findByIsContributionEnabledTrue();
        List<VdfFamilyMonthlySummaryResponse> summaries = new ArrayList<>();

        for (VdfFamilyConfig family : activeFamilies) {
            VdfFamilyMonthlySummaryResponse summary = new VdfFamilyMonthlySummaryResponse();
            summary.setFamilyConfigId(family.getId());
            summary.setFamilyHeadName(family.getFamilyHeadName());
            summary.setMemberPhone(family.getMember().getPhone());
            summary.setMonthlyAmount(family.getMonthlyAmount());

            // Get all contributions for this family in the year
            List<VdfContribution> contributions = contributionRepository
                    .findByFamilyConfigIdAndYear(family.getId(), year);

            Boolean[] paidMonths = new Boolean[12];
            Arrays.fill(paidMonths, false);

            int paidCount = 0;
            BigDecimal totalPaid = BigDecimal.ZERO;

            for (VdfContribution contribution : contributions) {
                int monthIndex = contribution.getMonth() - 1;
                paidMonths[monthIndex] = true;
                paidCount++;
                totalPaid = totalPaid.add(contribution.getAmount());
            }

            summary.setPaidMonths(paidMonths);
            summary.setTotalPaidMonths(paidCount);
            summary.setTotalPendingMonths(12 - paidCount);
            summary.setTotalPaid(totalPaid);
            summary.setTotalDue(family.getMonthlyAmount().multiply(new BigDecimal(12 - paidCount)));

            summaries.add(summary);
        }

        return summaries;
    }

    public BigDecimal calculateMemberVdfDues(UUID memberId) {
        Optional<VdfFamilyConfig> config = familyConfigRepository.findByMemberId(memberId);

        if (config.isEmpty() || !config.get().getIsContributionEnabled()) {
            return BigDecimal.ZERO;
        }

        int currentYear = LocalDate.now().getYear();
        List<VdfContribution> contributions = contributionRepository
                .findByFamilyConfigIdAndYear(config.get().getId(), currentYear);

        int paidMonths = contributions.size();
        int pendingMonths = 12 - paidMonths;

        return config.get().getMonthlyAmount().multiply(new BigDecimal(pendingMonths));
    }

    // ==================== REPORTS & SUMMARY ====================

    public VdfSummaryResponse getSummary() {
        int currentYear = LocalDate.now().getYear();

        VdfSummaryResponse summary = new VdfSummaryResponse();
        summary.setTotalFamilies(Math.toIntExact(familyConfigRepository.count()));
        summary.setActiveContributors(Math.toIntExact(familyConfigRepository.countActiveContributors()));
        summary.setTotalCollected(depositRepository.getTotalDeposits()
                .add(contributionRepository.getTotalByYear(currentYear)));
        summary.setTotalExpenses(expenseRepository.getTotalByYear(currentYear));
        summary.setCurrentBalance(summary.getTotalCollected().subtract(summary.getTotalExpenses()));
        summary.setCurrentYear(currentYear);

        return summary;
    }

    public List<VdfMonthlyReportResponse> getMonthlyReport(Integer year) {
        List<VdfMonthlyReportResponse> reports = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            VdfMonthlyReportResponse report = new VdfMonthlyReportResponse();
            report.setYear(year);
            report.setMonth(month);
            report.setMonthName(Month.of(month).name());

            BigDecimal contributions = contributionRepository.getTotalByYearAndMonth(year, month);
            BigDecimal expenses = expenseRepository.getTotalByYearAndMonth(year, month);

            report.setTotalContributions(contributions);
            report.setTotalExpenses(expenses);

            // Calculate families paid/pending
            List<VdfFamilyConfig> activeFamilies = familyConfigRepository.findByIsContributionEnabledTrue();
            int familiesPaid = 0;

            for (VdfFamilyConfig family : activeFamilies) {
                Optional<VdfContribution> contrib = contributionRepository
                        .findByFamilyConfigIdAndYearAndMonth(family.getId(), year, month);
                if (contrib.isPresent()) {
                    familiesPaid++;
                }
            }

            report.setFamiliesPaid(familiesPaid);
            report.setFamiliesPending(activeFamilies.size() - familiesPaid);

            reports.add(report);
        }

        return reports;
    }

    // ==================== CONVERSION METHODS ====================

    private VdfDepositResponse convertDepositToResponse(VdfDeposit deposit) {
        VdfDepositResponse response = new VdfDepositResponse();
        response.setId(deposit.getId());
        response.setDepositDate(deposit.getDepositDate());
        response.setAmount(deposit.getAmount());
        response.setSourceType(deposit.getSourceType());
        response.setSourceName(deposit.getSourceName());
        response.setYear(deposit.getYear());
        response.setNotes(deposit.getNotes());

        if (deposit.getMember() != null) {
            response.setMemberName(deposit.getMember().getFirstName() + " " +
                    deposit.getMember().getLastName());
        }

        return response;
    }

    private VdfExpenseResponse convertExpenseToResponse(VdfExpense expense) {
        VdfExpenseResponse response = new VdfExpenseResponse();
        response.setId(expense.getId());
        response.setExpenseDate(expense.getExpenseDate());
        response.setAmount(expense.getAmount());
        response.setCategory(expense.getCategory());
        response.setDescription(expense.getDescription());
        response.setYear(expense.getYear());
        response.setMonth(expense.getMonth());
        response.setNotes(expense.getNotes());
        return response;
    }
}