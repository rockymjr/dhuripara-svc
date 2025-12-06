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
    private final VdfMonthlyConfigRepository monthlyConfigRepository;
    private final VdfFamilyExemptionRepository exemptionRepository;
    private final VdfContributionRepository contributionRepository;
    private final VdfExpenseRepository expenseRepository;
    private final MemberRepository memberRepository;

    // ============================================
    // FAMILY CONFIG MANAGEMENT
    // ============================================

    @Transactional
    public VdfFamilyConfig registerFamily(VdfFamilyConfigRequest request) {
        log.info("Registering family for member: {}", request.getMemberId());

        if (familyConfigRepository.existsByMemberId(request.getMemberId())) {
            throw new BusinessException("Member is already registered in VDF");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        VdfFamilyConfig config = new VdfFamilyConfig();
        config.setMember(member);
        config.setFamilySize(request.getFamilySize());
        config.setJoinedDate(request.getJoinedDate() != null ? request.getJoinedDate() : LocalDate.now());
        config.setNotes(request.getNotes());
        config.setIsActive(true);

        return familyConfigRepository.save(config);
    }

    @Transactional
    public VdfFamilyConfig updateFamily(UUID familyId, VdfFamilyConfigRequest request) {
        VdfFamilyConfig config = familyConfigRepository.findById(familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Family not found"));

        config.setFamilySize(request.getFamilySize());
        if (request.getJoinedDate() != null) {
            config.setJoinedDate(request.getJoinedDate());
        }
        config.setNotes(request.getNotes());

        return familyConfigRepository.save(config);
    }

    public List<VdfFamilyConfig> getAllActiveFamilies() {
        return familyConfigRepository.findByIsActiveTrue();
    }

    // ============================================
    // MONTHLY CONFIG MANAGEMENT
    // ============================================

    @Transactional
    public VdfMonthlyConfig createOrUpdateMonthlyConfig(VdfMonthlyConfigRequest request) {
        log.info("Creating/updating monthly config for: {}", request.getMonthYear());

        VdfMonthlyConfig config = monthlyConfigRepository
                .findByMonthYear(request.getMonthYear())
                .orElse(new VdfMonthlyConfig());

        config.setMonthYear(request.getMonthYear());
        config.setRequiredAmount(request.getRequiredAmount());
        config.setDescription(request.getDescription());
        config.setIsActive(true);

        return monthlyConfigRepository.save(config);
    }

    public List<VdfMonthlyConfig> getAllMonthlyConfigs() {
        return monthlyConfigRepository.findByIsActiveTrueOrderByMonthYearDesc();
    }

    // ============================================
    // EXEMPTION MANAGEMENT
    // ============================================

    @Transactional
    public VdfFamilyExemption addExemption(VdfExemptionRequest request, UUID adminUserId) {
        log.info("Adding exemption for family: {} for month: {}", request.getFamilyId(), request.getMonthYear());

        if (exemptionRepository.existsByFamilyIdAndMonthYear(request.getFamilyId(), request.getMonthYear())) {
            throw new BusinessException("Exemption already exists for this family and month");
        }

        VdfFamilyConfig family = familyConfigRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new ResourceNotFoundException("Family not found"));

        VdfFamilyExemption exemption = new VdfFamilyExemption();
        exemption.setFamily(family);
        exemption.setMonthYear(request.getMonthYear());
        exemption.setReason(request.getReason());
        // Set exempted by if needed
        // exemption.setExemptedBy(adminUser);

        return exemptionRepository.save(exemption);
    }

    @Transactional
    public void removeExemption(UUID exemptionId) {
        exemptionRepository.deleteById(exemptionId);
    }

    // ============================================
    // CONTRIBUTION MANAGEMENT
    // ============================================

    @Transactional
    public VdfContribution recordContribution(VdfContributionRequest request) {
        log.info("Recording contribution for family: {}", request.getFamilyId());

        VdfFamilyConfig family = familyConfigRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new ResourceNotFoundException("Family not found"));

        // Validate allocations
        BigDecimal totalAllocated = request.getMonthAllocations().stream()
                .map(VdfContributionRequest.MonthAllocationDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAllocated.compareTo(request.getAmount()) != 0) {
            throw new BusinessException("Total allocated amount must equal payment amount");
        }

        VdfContribution contribution = new VdfContribution();
        contribution.setFamily(family);
        contribution.setAmount(request.getAmount());
        contribution.setPaymentDate(request.getPaymentDate());
        contribution.setNotes(request.getNotes());

        // Convert allocations
        List<VdfContribution.MonthAllocation> allocations = request.getMonthAllocations().stream()
                .map(dto -> new VdfContribution.MonthAllocation(dto.getMonth(), dto.getAmount()))
                .collect(Collectors.toList());
        contribution.setMonthAllocations(allocations);

        return contributionRepository.save(contribution);
    }

    public List<VdfContribution> getContributionsByFamily(UUID familyId) {
        return contributionRepository.findByFamilyIdOrderByPaymentDateDesc(familyId);
    }

    // ============================================
    // EXPENSE MANAGEMENT
    // ============================================

    @Transactional
    public VdfExpense recordExpense(VdfExpenseRequest request) {
        log.info("Recording expense: {}", request.getDescription());

        VdfExpense expense = new VdfExpense();
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setNotes(request.getNotes());

        return expenseRepository.save(expense);
    }

    public List<VdfExpense> getAllExpenses() {
        return expenseRepository.findAllByOrderByExpenseDateDesc();
    }

    // ============================================
    // REPORTING
    // ============================================

    public VdfBalanceSummaryResponse getBalanceSummary() {
        BigDecimal totalContributions = contributionRepository.getTotalContributions();
        BigDecimal totalExpenses = expenseRepository.getTotalExpenses();

        VdfBalanceSummaryResponse response = new VdfBalanceSummaryResponse();
        response.setTotalContributions(totalContributions);
        response.setTotalExpenses(totalExpenses);
        response.setCurrentBalance(totalContributions.subtract(totalExpenses));

        return response;
    }

    public List<VdfFamilyMonthlySummaryResponse> getMonthlyReport(String monthYear) {
        List<VdfFamilyMonthlySummaryResponse> report = new ArrayList<>();

        VdfMonthlyConfig monthlyConfig = monthlyConfigRepository.findByMonthYear(monthYear)
                .orElseThrow(() -> new ResourceNotFoundException("Monthly config not found for " + monthYear));

        List<VdfFamilyConfig> families = familyConfigRepository.findByIsActiveTrue();

        for (VdfFamilyConfig family : families) {
            VdfFamilyMonthlySummaryResponse summary = new VdfFamilyMonthlySummaryResponse();
            summary.setFamilyId(family.getId());
            summary.setMemberId(family.getMember().getId());
            summary.setMemberName(family.getMember().getFirstName() + " " + family.getMember().getLastName());
            summary.setPhone(family.getMember().getPhone());
            summary.setMonthYear(monthYear);
            summary.setRequiredAmount(monthlyConfig.getRequiredAmount());

            // Check exemption
            boolean isExempted = exemptionRepository.existsByFamilyIdAndMonthYear(family.getId(), monthYear);
            summary.setIsExempted(isExempted);

            // Get paid amount
            BigDecimal paidAmount = contributionRepository.getPaidAmountForFamilyMonth(family.getId(), monthYear);
            summary.setPaidAmount(paidAmount);

            // Calculate due
            BigDecimal dueAmount = isExempted ? BigDecimal.ZERO :
                    monthlyConfig.getRequiredAmount().subtract(paidAmount).max(BigDecimal.ZERO);
            summary.setDueAmount(dueAmount);

            report.add(summary);
        }

        return report;
    }

    public VdfSummaryResponse getPublicSummary() {
        log.info("Getting VDF public summary");

        BigDecimal totalContributions = contributionRepository.getTotalContributions();
        BigDecimal totalExpenses = expenseRepository.getTotalExpenses();

        long totalFamiliesCount = familyConfigRepository.count();
        long activeFamiliesCount = familyConfigRepository.findByIsActiveTrue().size();

        VdfSummaryResponse response = new VdfSummaryResponse();
        response.setTotalFamilies((int) totalFamiliesCount);
        response.setActiveFamilies((int) activeFamiliesCount);
        response.setTotalCollected(totalContributions);
        response.setTotalExpenses(totalExpenses);
        response.setCurrentBalance(totalContributions.subtract(totalExpenses));
        response.setCurrentYear(String.valueOf(java.time.LocalDate.now().getYear()));

        return response;
    }
}