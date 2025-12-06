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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VdfService {

    private final VdfFamilyConfigRepository familyConfigRepository;
    private final VdfContributionRepository contributionRepository;
    private final VdfDepositRepository depositRepository;
    private final VdfExpenseRepository expenseRepository;
    private final MemberRepository memberRepository;

    // ============================================
    // FAMILY CONFIG MANAGEMENT
    // ============================================

    @Transactional
    public VdfFamilyConfig createFamilyConfig(VdfFamilyConfigRequest request) {
        log.info("Creating VDF family config for member: {}", request.getMemberId());

        if (familyConfigRepository.existsByMemberId(request.getMemberId())) {
            throw new BusinessException("This member is already registered in VDF");
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

        return familyConfigRepository.save(config);
    }

    @Transactional
    public VdfFamilyConfig updateFamilyConfig(UUID familyId, VdfFamilyConfigRequest request) {
        VdfFamilyConfig config = familyConfigRepository.findById(familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Family config not found"));

        config.setFamilyHeadName(request.getFamilyHeadName());
        config.setIsContributionEnabled(request.getIsContributionEnabled());
        config.setEffectiveFrom(request.getEffectiveFrom());
        config.setMonthlyAmount(request.getMonthlyAmount());
        config.setNotes(request.getNotes());

        return familyConfigRepository.save(config);
    }

    public List<VdfFamilyConfigResponse> getAllFamilies(boolean activeOnly) {
        List<VdfFamilyConfig> families = activeOnly
                ? familyConfigRepository.findByIsContributionEnabledTrue()
                : familyConfigRepository.findAllByOrderByFamilyHeadNameAsc();

        return families.stream()
                .map(this::convertToFamilyResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // CONTRIBUTION MANAGEMENT
    // ============================================

    @Transactional
    public VdfContribution recordContribution(VdfContributionRequest request) {
        log.info("Recording VDF contribution for family: {}", request.getFamilyConfigId());

        VdfFamilyConfig family = familyConfigRepository.findById(request.getFamilyConfigId())
                .orElseThrow(() -> new ResourceNotFoundException("Family config not found"));

        // Check if already paid for this month
        Optional<VdfContribution> existing = contributionRepository
                .findByFamilyConfigIdAndMonthAndYear(request.getFamilyConfigId(), request.getMonth(), request.getYear());

        if (existing.isPresent()) {
            throw new BusinessException("Contribution already recorded for this month");
        }

        VdfContribution contribution = new VdfContribution();
        contribution.setFamilyConfig(family);
        contribution.setMonth(request.getMonth());
        contribution.setYear(request.getYear());
        contribution.setAmount(request.getAmount());
        contribution.setPaymentDate(request.getPaymentDate());
        contribution.setNotes(request.getNotes());
        contribution.setCreatedBy("admin");

        return contributionRepository.save(contribution);
    }

    public VdfMonthlyMatrixResponse getMonthlyContributionMatrix(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        List<VdfFamilyConfig> enabledFamilies = familyConfigRepository.findByIsContributionEnabledTrue();
        List<VdfContribution> yearContributions = contributionRepository.findByYearOrderByMonthAsc(year);

        VdfMonthlyMatrixResponse response = new VdfMonthlyMatrixResponse();
        response.setYear(year);

        List<VdfMonthlyMatrixResponse.FamilyRow> rows = new ArrayList<>();

        for (VdfFamilyConfig family : enabledFamilies) {
            VdfMonthlyMatrixResponse.FamilyRow row = new VdfMonthlyMatrixResponse.FamilyRow();
            row.setFamilyId(family.getId());
            row.setFamilyHeadName(family.getFamilyHeadName());
            row.setMemberName(family.getMember().getFirstName() + " " + family.getMember().getLastName());
            row.setMonthlyAmount(family.getMonthlyAmount());

            Map<String, VdfMonthlyMatrixResponse.MonthStatus> months = new HashMap<>();
            BigDecimal totalPaid = BigDecimal.ZERO;
            int paidMonths = 0;

            for (int month = 1; month <= 12; month++) {
                final int currentMonth = month;
                Optional<VdfContribution> contribution = yearContributions.stream()
                        .filter(c -> c.getFamilyConfig().getId().equals(family.getId())
                                && c.getMonth().equals(currentMonth))
                        .findFirst();

                VdfMonthlyMatrixResponse.MonthStatus status = new VdfMonthlyMatrixResponse.MonthStatus();
                status.setPaid(contribution.isPresent());
                if (contribution.isPresent()) {
                    status.setAmount(contribution.get().getAmount());
                    status.setPaymentDate(contribution.get().getPaymentDate());
                    totalPaid = totalPaid.add(contribution.get().getAmount());
                    paidMonths++;
                } else {
                    status.setAmount(BigDecimal.ZERO);
                }

                months.put("month" + month, status);
            }

            row.setMonths(months);
            row.setTotalPaid(totalPaid);
            row.setTotalDue(family.getMonthlyAmount().multiply(new BigDecimal(12 - paidMonths)));
            row.setPaidMonths(paidMonths);
            row.setPendingMonths(12 - paidMonths);

            rows.add(row);
        }

        response.setFamilies(rows);
        return response;
    }

    // ============================================
    // DEPOSIT MANAGEMENT
    // ============================================

    @Transactional
    public VdfDeposit recordDeposit(VdfDepositRequest request) {
        log.info("Recording VDF deposit: {}", request.getDescription());

        VdfDeposit deposit = new VdfDeposit();
        deposit.setDepositDate(request.getDepositDate());
        deposit.setAmount(request.getAmount());
        deposit.setSourceType(request.getSourceType());
        deposit.setSourceName(request.getSourceName());
        deposit.setDescription(request.getDescription());
        deposit.setNotes(request.getNotes());
        deposit.setCreatedBy("admin");

        return depositRepository.save(deposit);
    }

    public List<VdfDeposit> getAllDeposits(Integer year) {
        if (year == null) {
            return depositRepository.findAllByOrderByDepositDateDesc();
        }
        return depositRepository.findByYear(year);
    }

    // ============================================
    // EXPENSE MANAGEMENT
    // ============================================

    @Transactional
    public VdfExpense recordExpense(VdfExpenseRequest request) {
        log.info("Recording VDF expense: {}", request.getDescription());

        VdfExpense expense = new VdfExpense();
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setNotes(request.getNotes());
        expense.setCreatedBy("admin");

        return expenseRepository.save(expense);
    }

    public List<VdfExpense> getAllExpenses(Integer year) {
        if (year == null) {
            return expenseRepository.findAllByOrderByExpenseDateDesc();
        }
        return expenseRepository.findByYear(year);
    }

    // ============================================
    // REPORTING
    // ============================================

    public VdfSummaryResponse getSummary() {
        BigDecimal totalContributions = contributionRepository.getTotalContributions();
        BigDecimal totalDeposits = depositRepository.getTotalDeposits();
        BigDecimal totalIncome = totalContributions.add(totalDeposits);
        BigDecimal totalExpenses = expenseRepository.getTotalExpenses();
        BigDecimal currentBalance = totalIncome.subtract(totalExpenses);

        long totalFamilies = familyConfigRepository.count();
        long activeFamilies = familyConfigRepository.findByIsContributionEnabledTrue().size();

        VdfSummaryResponse response = new VdfSummaryResponse();
        response.setTotalFamilies((int) totalFamilies);
        response.setActiveFamilies((int) activeFamilies);
        response.setTotalContributions(totalContributions);
        response.setTotalDeposits(totalDeposits);
        response.setTotalExpenses(totalExpenses);
        response.setCurrentBalance(currentBalance);
        response.setCurrentYear(String.valueOf(LocalDate.now().getYear()));

        return response;
    }

    public BigDecimal getMemberVdfDue(UUID memberId) {
        Optional<VdfFamilyConfig> configOpt = familyConfigRepository.findByMemberId(memberId);
        if (configOpt.isEmpty() || !configOpt.get().getIsContributionEnabled()) {
            return BigDecimal.ZERO;
        }

        VdfFamilyConfig config = configOpt.get();
        LocalDate effectiveFrom = config.getEffectiveFrom();
        LocalDate now = LocalDate.now();

        // Calculate total months from effective date
        int totalMonths = (now.getYear() - effectiveFrom.getYear()) * 12
                + (now.getMonthValue() - effectiveFrom.getMonthValue()) + 1;

        BigDecimal totalDue = config.getMonthlyAmount().multiply(new BigDecimal(totalMonths));

        // Calculate total paid
        List<VdfContribution> contributions = contributionRepository
                .findByFamilyConfigIdOrderByYearDescMonthDesc(config.getId());
        BigDecimal totalPaid = contributions.stream()
                .map(VdfContribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalDue.subtract(totalPaid).max(BigDecimal.ZERO);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private VdfFamilyConfigResponse convertToFamilyResponse(VdfFamilyConfig config) {
        VdfFamilyConfigResponse response = new VdfFamilyConfigResponse();
        response.setId(config.getId());
        response.setMemberId(config.getMember().getId());
        response.setMemberName(config.getMember().getFirstName() + " " + config.getMember().getLastName());
        response.setFamilyHeadName(config.getFamilyHeadName());
        response.setIsContributionEnabled(config.getIsContributionEnabled());
        response.setEffectiveFrom(config.getEffectiveFrom());
        response.setMonthlyAmount(config.getMonthlyAmount());
        response.setNotes(config.getNotes());

        // Calculate summary
        if (config.getIsContributionEnabled()) {
            List<VdfContribution> contributions = contributionRepository
                    .findByFamilyConfigIdOrderByYearDescMonthDesc(config.getId());

            int currentYear = LocalDate.now().getYear();
            BigDecimal yearlyPaid = contributions.stream()
                    .filter(c -> c.getYear().equals(currentYear))
                    .map(VdfContribution::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.setTotalPaidMonths(contributions.size());
            response.setTotalAmountPaid(yearlyPaid);

            BigDecimal yearlyDue = getMemberVdfDue(config.getMember().getId());
            response.setTotalAmountDue(yearlyDue);
        }

        return response;
    }
}