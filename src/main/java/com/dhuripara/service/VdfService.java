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
    private final VdfExpenseCategoryRepository expenseCategoryRepository;
    private final VdfDepositCategoryRepository depositCategoryRepository;

    // ==================== DEPOSITS ====================

    @Transactional
    public VdfDepositResponse createDeposit(VdfDepositRequest request) {
        log.info("Creating VDF deposit from: {}", request.getSourceName());

        VdfDepositCategory category = depositCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Deposit category not found"));

        VdfDeposit deposit = new VdfDeposit();
        deposit.setDepositDate(request.getDepositDate());
        deposit.setAmount(request.getAmount());
        deposit.setSourceType(request.getSourceType());
        deposit.setSourceName(request.getSourceName());
        deposit.setCategory(category);
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

        VdfExpenseCategory category = expenseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        VdfExpense expense = new VdfExpense();
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAmount(request.getAmount());
        expense.setCategory(category);
        expense.setDescription(request.getDescription());
        expense.setNotes(request.getNotes());
        expense.setYear(request.getExpenseDate().getYear());
        expense.setMonth(request.getExpenseDate().getMonth().getValue());

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

    public List<VdfFamilyConfigResponse> getAllFamilies(Boolean activeOnly) {
        List<VdfFamilyConfig> families;
        if (Boolean.TRUE.equals(activeOnly)) {
            families = familyConfigRepository.findByIsContributionEnabledTrue();
        } else {
            families = familyConfigRepository.findAll();
        }
        
        int currentYear = LocalDate.now().getYear();
        return families.stream()
            .map(family -> convertFamilyToResponse(family, currentYear))
            .collect(Collectors.toList());
    }

    @Transactional
    public VdfFamilyConfigResponse updateFamilyConfig(UUID id, VdfFamilyConfigRequest request) {
        log.info("Updating family config: {}", id);

        VdfFamilyConfig config = familyConfigRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Family config not found"));

        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        config.setMember(member);
        config.setFamilyHeadName(request.getFamilyHeadName());
        config.setIsContributionEnabled(request.getIsContributionEnabled());
        config.setEffectiveFrom(request.getEffectiveFrom());
        config.setMonthlyAmount(request.getMonthlyAmount());
        config.setNotes(request.getNotes());

        VdfFamilyConfig updated = familyConfigRepository.save(config);
        return convertFamilyToResponse(updated, LocalDate.now().getYear());
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

        // Also create a deposit entry for the villager contribution
        VdfDepositCategory villagerCategory = depositCategoryRepository
                .findByIsActiveTrueOrderByCategoryNameAsc()
                .stream()
                .filter(cat -> cat.getCategoryName().equalsIgnoreCase("Villager Contribution"))
                .findFirst()
                .orElse(null);

        if (villagerCategory != null) {
            VdfDeposit deposit = new VdfDeposit();
            deposit.setDepositDate(request.getPaymentDate());
            deposit.setAmount(request.getAmount());
            deposit.setSourceType("VILLAGER");
            deposit.setSourceName(familyConfig.getMember().getFirstName() + " " + familyConfig.getMember().getLastName() + " - Month " + request.getMonth());
            deposit.setMember(familyConfig.getMember());
            deposit.setCategory(villagerCategory);
            deposit.setYear(request.getYear());
            deposit.setNotes(request.getNotes());
            
            depositRepository.save(deposit);
            log.info("Created corresponding deposit for contribution: {}", deposit.getId());
        }
    }

    public List<VdfContributionResponse> getFamilyContributions(UUID familyConfigId, Integer year) {
        log.info("Getting contributions for family config: {} year {}", familyConfigId, year);
        
        List<VdfContribution> contributions = contributionRepository.findByFamilyConfigIdAndYear(familyConfigId, year);
        return contributions.stream()
                .map(this::mapToContributionResponse)
                .toList();
    }

    private VdfContributionResponse mapToContributionResponse(VdfContribution contribution) {
        VdfContributionResponse response = new VdfContributionResponse();
        response.setId(contribution.getId());
        response.setFamilyId(contribution.getFamilyConfig().getId());
        response.setMemberName(contribution.getFamilyConfig().getMember().getFirstName() + " " + 
                               contribution.getFamilyConfig().getMember().getLastName());
        response.setMonth(contribution.getMonth());
        response.setYear(contribution.getYear());
        response.setAmount(contribution.getAmount());
        response.setPaymentDate(contribution.getPaymentDate());
        response.setNotes(contribution.getNotes());
        return response;
    }

    public void recordBulkContributions(com.dhuripara.dto.request.VdfBulkContributionRequest request) {
        log.info("Recording bulk contributions for family config: {} year {}", request.getFamilyConfigId(), request.getYear());

        VdfFamilyConfig familyConfig = familyConfigRepository.findById(request.getFamilyConfigId())
                .orElseThrow(() -> new ResourceNotFoundException("Family config not found"));

        if (!familyConfig.getIsContributionEnabled()) {
            throw new BusinessException("Contributions are not enabled for this family");
        }

        // process each month
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        StringBuilder monthsList = new StringBuilder();
        
        for (com.dhuripara.dto.request.VdfBulkContributionRequest.MonthlyContributionInput contrib : request.getContributions()) {
            Integer month = contrib.getMonth();
            java.math.BigDecimal amount = contrib.getAmount() == null ? java.math.BigDecimal.ZERO : contrib.getAmount();

            if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                continue; // skip zero amounts
            }

            total = total.add(amount);
            if (monthsList.length() > 0) monthsList.append(", ");
            monthsList.append(month);

            Optional<VdfContribution> existing = contributionRepository
                    .findByFamilyConfigIdAndYearAndMonth(request.getFamilyConfigId(), request.getYear(), month);

            if (existing.isPresent()) {
                VdfContribution c = existing.get();
                c.setAmount(amount);
                c.setPaymentDate(request.getPaymentDate());
                c.setNotes(request.getNotes());
                contributionRepository.save(c);
            } else {
                VdfContribution c = new VdfContribution();
                c.setFamilyConfig(familyConfig);
                c.setYear(request.getYear());
                c.setMonth(month);
                c.setAmount(amount);
                c.setPaymentDate(request.getPaymentDate());
                c.setNotes(request.getNotes());
                contributionRepository.save(c);
            }
        }

        if (total.compareTo(java.math.BigDecimal.ZERO) > 0) {
            // create single deposit for the total amount
            VdfDepositCategory villagerCategory = depositCategoryRepository
                    .findByIsActiveTrueOrderByCategoryNameAsc()
                    .stream()
                    .filter(cat -> cat.getCategoryName().equalsIgnoreCase("Villager Contribution") || cat.getCategoryName().equalsIgnoreCase("Monthly Contribution"))
                    .findFirst()
                    .orElse(null);

            VdfDeposit deposit = new VdfDeposit();
            deposit.setDepositDate(request.getPaymentDate());
            deposit.setAmount(total);
            deposit.setSourceType("VILLAGER");
            deposit.setSourceName(familyConfig.getMember().getFirstName() + " " + familyConfig.getMember().getLastName() + " - Bulk " + request.getYear());
            deposit.setMember(familyConfig.getMember());
            deposit.setCategory(villagerCategory);
            deposit.setYear(request.getYear());
            deposit.setNotes("Bulk contribution months: " + monthsList.toString() + (request.getNotes() != null ? (" - " + request.getNotes()) : ""));
            depositRepository.save(deposit);
            log.info("Created bulk deposit for contributions: {}", deposit.getId());
        }
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
        if (expense.getCategory() != null) {
            response.setCategoryId(expense.getCategory().getId());
            response.setCategoryName(expense.getCategory().getCategoryName());
        }
        response.setDescription(expense.getDescription());
        response.setNotes(expense.getNotes());
        return response;
    }

    private VdfFamilyConfigResponse convertFamilyToResponse(VdfFamilyConfig family, Integer year) {
        VdfFamilyConfigResponse response = new VdfFamilyConfigResponse();
        response.setId(family.getId());
        response.setMemberId(family.getMember().getId());
        response.setMemberName(family.getMember().getFirstName() + " " + family.getMember().getLastName());
        response.setFamilyHeadName(family.getFamilyHeadName());
        response.setIsContributionEnabled(family.getIsContributionEnabled());
        response.setEffectiveFrom(family.getEffectiveFrom());
        response.setMonthlyAmount(family.getMonthlyAmount());
        response.setNotes(family.getNotes());

        // Calculate paid and pending months for the year
        if (Boolean.TRUE.equals(family.getIsContributionEnabled())) {
            List<VdfContribution> contributions = contributionRepository
                .findByFamilyConfigIdAndYear(family.getId(), year);
            
            response.setTotalPaidMonths(contributions.size());
            response.setTotalPendingMonths(12 - contributions.size());
            
            BigDecimal totalPaid = contributions.stream()
                .map(VdfContribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotalAmountPaid(totalPaid);
            
            BigDecimal totalDue = family.getMonthlyAmount()
                .multiply(new BigDecimal(12 - contributions.size()));
            response.setTotalAmountDue(totalDue);
        } else {
            response.setTotalPaidMonths(0);
            response.setTotalPendingMonths(0);
            response.setTotalAmountPaid(BigDecimal.ZERO);
            response.setTotalAmountDue(BigDecimal.ZERO);
        }

        return response;
    }

    public List<VdfExpenseCategory> getExpenseCategories() {
        return expenseCategoryRepository.findAll();
    }

    public List<VdfDepositCategoryResponse> getDepositCategories() {
        return depositCategoryRepository.findByIsActiveTrueOrderByCategoryNameAsc()
                .stream()
                .map(this::convertCategoryToResponse)
                .collect(Collectors.toList());
    }

    private VdfDepositCategoryResponse convertCategoryToResponse(VdfDepositCategory category) {
        return VdfDepositCategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .build();
    }
}