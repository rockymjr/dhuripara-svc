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
    private final VdfFamilyExemptionRepository vdfFamilyExemptionRepository;
    private final VdfMonthlyConfigRepository vdfMonthlyConfigRepository;
    private final VdfNotificationService notificationService;

    // ==================== DEPOSITS ====================

    @Transactional
    public VdfDepositResponse createDeposit(VdfDepositRequest request) {
        log.info("Creating VDF deposit from: {}", request.getSourceName());

        VdfDepositCategory category = depositCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Deposit category not found"));

        VdfDeposit deposit = new VdfDeposit();
        deposit.setDepositDate(request.getDepositDate());
        deposit.setAmount(request.getAmount());
        deposit.setSourceName(request.getSourceName());
        deposit.setSourceNameBn(request.getSourceNameBn());
        deposit.setCategory(category);
        deposit.setNotes(request.getNotes());

        if (request.getMemberId() != null) {
            Member member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
            deposit.setMember(member);
        }

        VdfDeposit saved = depositRepository.save(deposit);
        
        // Create notification if requested
        if (Boolean.TRUE.equals(request.getSendNotification())) {
            try {
                if (request.getMemberId() != null) {
                    // Member-specific deposit - notify only that member
                    String title = "New Deposit Recorded";
                    String titleBn = "নতুন জমা রেকর্ড করা হয়েছে";
                    String message = String.format("A deposit of ₹%s has been recorded in your account.", 
                        saved.getAmount());
                    String messageBn = String.format("আপনার অ্যাকাউন্টে ₹%s এর একটি জমা রেকর্ড করা হয়েছে।", 
                        saved.getAmount());
                    notificationService.createNotificationForMember(
                        request.getMemberId(), title, titleBn, message, messageBn, "DEPOSIT", saved.getId()
                    );
                } else {
                    // Non-member deposit - notify everyone
                    String title = "New Deposit Recorded";
                    String titleBn = "নতুন জমা রেকর্ড করা হয়েছে";
                    String message = String.format("A new deposit of ₹%s has been recorded. Category: %s", 
                        saved.getAmount(), category.getCategoryName());
                    String messageBn = String.format("₹%s এর একটি নতুন জমা রেকর্ড করা হয়েছে। বিভাগ: %s", 
                        saved.getAmount(), 
                        category.getCategoryNameBn() != null ? category.getCategoryNameBn() : category.getCategoryName());
                    notificationService.createNotificationForAllMembers(
                        title, titleBn, message, messageBn, "DEPOSIT", saved.getId()
                    );
                }
            } catch (Exception e) {
                log.error("Error creating notification for deposit: {}", saved.getId(), e);
                // Don't fail the deposit creation if notification fails
            }
        }
        
        return convertDepositToResponse(saved);
        }

    public List<VdfDepositResponse> getDepositsByYear(Integer year) {
        return depositRepository.findByYearOrderByDepositDateDesc(year).stream()
                .map(this::convertDepositToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDeposit(UUID id) {
        VdfDeposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        depositRepository.deleteById(id);
        log.info("VDF deposit deleted: {} (Amount: {})", id, deposit.getAmount());
    }

    @Transactional
    public VdfDepositResponse updateDeposit(UUID id, VdfDepositRequest request) {
        log.info("Updating VDF deposit: {}", id);

        VdfDeposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));

        VdfDepositCategory category = depositCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Deposit category not found"));

        deposit.setDepositDate(request.getDepositDate());
        deposit.setAmount(request.getAmount());
        deposit.setSourceName(request.getSourceName());
        deposit.setSourceNameBn(request.getSourceNameBn());
        deposit.setCategory(category);
        deposit.setNotes(request.getNotes());

        if (request.getMemberId() != null) {
            Member member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
            deposit.setMember(member);
        } else {
            deposit.setMember(null);
        }

        VdfDeposit updated = depositRepository.save(deposit);
        log.info("VDF deposit updated: {}", id);
        return convertDepositToResponse(updated);
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
        expense.setDescriptionBn(request.getDescriptionBn());
        expense.setNotes(request.getNotes());
        expense.setYear(request.getExpenseDate().getYear());
        expense.setMonth(request.getExpenseDate().getMonth().getValue());

        VdfExpense saved = expenseRepository.save(expense);
        
        // Create notification if requested
        if (Boolean.TRUE.equals(request.getSendNotification())) {
            try {
                // Expense notification goes to everyone
                String title = "New Expense Recorded";
                String titleBn = "নতুন খরচ রেকর্ড করা হয়েছে";
                String message = String.format("A new expense of ₹%s has been recorded. Category: %s. Description: %s", 
                    saved.getAmount(), category.getCategoryName(), saved.getDescription());
                String messageBn = String.format("₹%s এর একটি নতুন খরচ রেকর্ড করা হয়েছে। বিভাগ: %s। বিবরণ: %s", 
                    saved.getAmount(), 
                    category.getCategoryNameBn() != null ? category.getCategoryNameBn() : category.getCategoryName(),
                    saved.getDescriptionBn() != null ? saved.getDescriptionBn() : saved.getDescription());
                notificationService.createNotificationForAllMembers(
                    title, titleBn, message, messageBn, "EXPENSE", saved.getId()
                );
            } catch (Exception e) {
                log.error("Error creating notification for expense: {}", saved.getId(), e);
                // Don't fail the expense creation if notification fails
            }
        }
        
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
            // result[0] is VdfExpenseCategory object, not String
            VdfExpenseCategory category = (VdfExpenseCategory) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            String categoryName = category.getCategoryName();
            categoryExpenses.put(categoryName, amount);
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

    @Transactional
    public void createExemption(com.dhuripara.dto.request.VdfExemptionRequest request) {
        log.info("Creating exemption for family: {} month: {}", request.getFamilyId(), request.getMonthYear());

        VdfFamilyConfig family = familyConfigRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new ResourceNotFoundException("Family config not found"));

        if (vdfFamilyExemptionRepository.existsByFamilyIdAndMonthYear(request.getFamilyId(), request.getMonthYear())) {
            throw new BusinessException("Exemption already exists for this family and month");
        }

        VdfFamilyExemption ex = new VdfFamilyExemption();
        ex.setFamily(family);
        ex.setMonthYear(request.getMonthYear());
        ex.setReason(request.getReason());
        // exemptedBy left null (could be set to current admin if available)

        vdfFamilyExemptionRepository.save(ex);
    }

    @Transactional
    public void deleteExemption(UUID familyId, String monthYear) {
        log.info("Deleting exemption for family: {} month: {}", familyId, monthYear);
        VdfFamilyExemption ex = vdfFamilyExemptionRepository.findByFamilyIdAndMonthYear(familyId, monthYear)
                .orElseThrow(() -> new ResourceNotFoundException("Exemption not found"));
        vdfFamilyExemptionRepository.delete(ex);
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
                deposit.setSourceName(familyConfig.getMember().getFirstName() + " " + familyConfig.getMember().getLastName() + " - Month " + request.getMonth());
                // build bn source name from member bn fields if available
                String bnFirst = familyConfig.getMember().getFirstNameBn();
                String bnLast = familyConfig.getMember().getLastNameBn();
                if (bnFirst != null && !bnFirst.isBlank()) {
                    deposit.setSourceNameBn((bnFirst + " " + (bnLast != null ? bnLast : "")).trim() + " - Month " + request.getMonth());
                }
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
        response.setMemberName(com.dhuripara.util.NameUtil.buildMemberName(contribution.getFamilyConfig().getMember()));
        response.setMonth(contribution.getMonth());
        response.setYear(contribution.getYear());
        response.setAmount(contribution.getAmount());
        response.setPaymentDate(contribution.getPaymentDate());
        response.setNotes(contribution.getNotes());
        return response;
    }

    @Transactional
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

            Optional<VdfContribution> existing = contributionRepository
                    .findByFamilyConfigIdAndYearAndMonth(request.getFamilyConfigId(), request.getYear(), month);

            if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                // Delete contribution if it exists and amount is zero
                if (existing.isPresent()) {
                    log.info("Deleting contribution for family {} month {} year {}", request.getFamilyConfigId(), month, request.getYear());
                    contributionRepository.delete(existing.get());
                }
                continue; // skip further processing for zero amounts
            }

            total = total.add(amount);
            if (monthsList.length() > 0) monthsList.append(", ");
            monthsList.append(month);

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
            deposit.setSourceName(familyConfig.getMember().getFirstName() + " " + familyConfig.getMember().getLastName() + " - Bulk " + request.getYear());
            String bnF = familyConfig.getMember().getFirstNameBn();
            String bnL = familyConfig.getMember().getLastNameBn();
            if (bnF != null && !bnF.isBlank()) {
                deposit.setSourceNameBn((bnF + " " + (bnL != null ? bnL : "")).trim() + " - Bulk " + request.getYear());
            }
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
        LocalDate today = LocalDate.now();

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

            // Fetch exemptions for this family for the year
            Boolean[] exemptedMonths = new Boolean[12];
            Arrays.fill(exemptedMonths, false);
            Map<String, Boolean> exemptionsMap = new HashMap<>();
            List<VdfFamilyExemption> exemptions = vdfFamilyExemptionRepository.findByFamilyId(family.getId());
            for (VdfFamilyExemption ex : exemptions) {
                String my = ex.getMonthYear();
                if (my != null && my.startsWith(String.valueOf(year))) {
                    try {
                        int month = Integer.parseInt(my.substring(5,7));
                        if (month >=1 && month <=12) {
                            exemptedMonths[month-1] = true;
                        }
                    } catch (Exception ignored) {}
                    exemptionsMap.put(my, true);
                }
            }

            // Calculate applicable months from effectiveFrom to current month
            int applicableMonths = calculateApplicableMonths(family.getEffectiveFrom(), year, today);

            summary.setPaidMonths(paidMonths);
            summary.setExemptedMonths(exemptedMonths);
            summary.setExemptionsMap(exemptionsMap);
            summary.setTotalPaidMonths(paidCount);

            int exemptCount = 0;
            for (boolean e : exemptedMonths) if (e) exemptCount++;

            int effectivePending = applicableMonths - paidCount - exemptCount;
            if (effectivePending < 0) effectivePending = 0;

            summary.setTotalPendingMonths(effectivePending);
            summary.setTotalPaid(totalPaid);
            summary.setTotalDue(family.getMonthlyAmount().multiply(new BigDecimal(effectivePending)));

            // All-time totals (since effectiveFrom to today)
            java.math.BigDecimal paidAll = contributionRepository.getTotalByFamily(family.getId());
            summary.setTotalPaidAllTime(paidAll);

            // compute all-time due by iterating months from effectiveFrom to today
            java.time.LocalDate start = family.getEffectiveFrom();
            if (start == null) {
                if (family.getCreatedAt() != null) start = family.getCreatedAt().toLocalDate();
                else start = LocalDate.of(2023, 6, 1);
            }
            java.math.BigDecimal requiredSum = java.math.BigDecimal.ZERO;
            java.time.LocalDate iter = java.time.LocalDate.of(start.getYear(), start.getMonthValue(), 1);
            while (!iter.isAfter(today)) {
                String monText = String.format("%04d-%02d", iter.getYear(), iter.getMonthValue());
                // skip exempted months
                if (!vdfFamilyExemptionRepository.existsByFamilyIdAndMonthYear(family.getId(), monText)) {
                    java.math.BigDecimal req = vdfMonthlyConfigRepository.findByMonthYear(monText)
                            .map(VdfMonthlyConfig -> VdfMonthlyConfig.getRequiredAmount())
                            .orElse(family.getMonthlyAmount());
                    requiredSum = requiredSum.add(req == null ? java.math.BigDecimal.ZERO : req);
                }
                iter = iter.plusMonths(1);
            }

            java.math.BigDecimal dueAll = requiredSum.subtract(paidAll == null ? java.math.BigDecimal.ZERO : paidAll);
            if (dueAll.compareTo(java.math.BigDecimal.ZERO) < 0) dueAll = java.math.BigDecimal.ZERO;
            summary.setTotalDueAllTime(dueAll);
            // Populate alias fields for frontend compatibility
            summary.setTotalAmountPaid(paidAll == null ? java.math.BigDecimal.ZERO : paidAll);
            summary.setTotalAmountDue(dueAll == null ? java.math.BigDecimal.ZERO : dueAll);

            summaries.add(summary);
        }

        return summaries;
    }

    public BigDecimal calculateMemberVdfDues(UUID memberId) {
        Optional<VdfFamilyConfig> config = familyConfigRepository.findByMemberId(memberId);

        if (config.isEmpty() || !config.get().getIsContributionEnabled()) {
            return BigDecimal.ZERO;
        }

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        List<VdfContribution> contributions = contributionRepository
                .findByFamilyConfigIdAndYear(config.get().getId(), currentYear);

        int paidMonths = contributions.size();
        
        // Calculate applicable months from effectiveFrom to current month
        int applicableMonths = calculateApplicableMonths(config.get().getEffectiveFrom(), currentYear, today);
        int pendingMonths = applicableMonths - paidMonths;
        if (pendingMonths < 0) pendingMonths = 0;

        return config.get().getMonthlyAmount().multiply(new BigDecimal(pendingMonths));
    }

    public com.dhuripara.dto.response.MemberVdfAccountResponse getMemberVdfAccount(UUID memberId) {
        com.dhuripara.dto.response.MemberVdfAccountResponse resp = new com.dhuripara.dto.response.MemberVdfAccountResponse();

        // find family config for member
        java.util.Optional<VdfFamilyConfig> cfgOpt = familyConfigRepository.findByMemberId(memberId);
        if (cfgOpt.isEmpty()) {
            resp.setTotalPaidAllTime(java.math.BigDecimal.ZERO);
            resp.setTotalDueAllTime(java.math.BigDecimal.ZERO);
            resp.setCurrentYearDue(java.math.BigDecimal.ZERO);
            resp.setContributions(java.util.List.of());
            return resp;
        }

        VdfFamilyConfig family = cfgOpt.get();
        UUID familyId = family.getId();

        // total paid all time
        java.math.BigDecimal paidAll = contributionRepository.getTotalByFamily(familyId);
        if (paidAll == null) paidAll = java.math.BigDecimal.ZERO;

        // compute required sum from effectiveFrom to today, skipping exemptions and using monthly config when present
        java.time.LocalDate start = family.getEffectiveFrom();
        if (start == null) {
            if (family.getCreatedAt() != null) start = family.getCreatedAt().toLocalDate();
            else start = java.time.LocalDate.of(2023, 6, 1);
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        java.math.BigDecimal requiredSum = java.math.BigDecimal.ZERO;
        java.time.LocalDate iter = java.time.LocalDate.of(start.getYear(), start.getMonthValue(), 1);
        while (!iter.isAfter(today)) {
            String monText = String.format("%04d-%02d", iter.getYear(), iter.getMonthValue());
            if (!vdfFamilyExemptionRepository.existsByFamilyIdAndMonthYear(familyId, monText)) {
                java.math.BigDecimal req = vdfMonthlyConfigRepository.findByMonthYear(monText)
                        .map(VdfMonthlyConfig -> VdfMonthlyConfig.getRequiredAmount())
                        .orElse(family.getMonthlyAmount());
                requiredSum = requiredSum.add(req == null ? java.math.BigDecimal.ZERO : req);
            }
            iter = iter.plusMonths(1);
        }

        java.math.BigDecimal dueAll = requiredSum.subtract(paidAll == null ? java.math.BigDecimal.ZERO : paidAll);
        if (dueAll.compareTo(java.math.BigDecimal.ZERO) < 0) dueAll = java.math.BigDecimal.ZERO;

        // current year due (use existing method)
        java.math.BigDecimal currentDue = calculateMemberVdfDues(memberId);

        // contributions list (all years)
        java.util.List<VdfContribution> contribs = contributionRepository.findAll().stream()
                .filter(c -> c.getFamilyConfig() != null && familyId.equals(c.getFamilyConfig().getId()))
                .sorted((a,b) -> {
                    int cmp = b.getYear().compareTo(a.getYear());
                    if (cmp != 0) return cmp;
                    return b.getMonth().compareTo(a.getMonth());
                })
                .toList();

        java.util.List<com.dhuripara.dto.response.VdfContributionResponse> contribResp = contribs.stream()
                .map(this::mapToContributionResponse)
                .toList();

        resp.setTotalPaidAllTime(paidAll);
        resp.setTotalDueAllTime(dueAll);
        resp.setCurrentYearDue(currentDue);
        resp.setContributions(contribResp);
        return resp;
    }

    private int calculateApplicableMonths(LocalDate effectiveFrom, Integer year, LocalDate today) {
        if (effectiveFrom == null) {
            effectiveFrom = LocalDate.of(year, 1, 1);
        }

        // If effectiveFrom is after today, no applicable months
        if (effectiveFrom.isAfter(today)) {
            return 0;
        }

        // If effectiveFrom year is different from requested year
        if (effectiveFrom.getYear() != year) {
            if (effectiveFrom.getYear() > year) {
                return 0; // Family starts after this year
            }
            // effectiveFrom is before the year, include all months up to current month
            if (today.getYear() == year) {
                return today.getMonthValue();
            } else {
                return 12; // This year is fully in the past
            }
        }

        // Both are in same year
        int startMonth = effectiveFrom.getMonthValue();
        int endMonth = today.getMonthValue();
        return endMonth - startMonth + 1;
    }

    // ==================== REPORTS & SUMMARY ====================

    public VdfSummaryResponse getSummary() {
        int currentYear = LocalDate.now().getYear();

        VdfSummaryResponse summary = new VdfSummaryResponse();
        summary.setTotalFamilies(Math.toIntExact(familyConfigRepository.count()));
        summary.setActiveContributors(Math.toIntExact(familyConfigRepository.countActiveContributors()));
        summary.setTotalCollected(depositRepository.getTotalDeposits());
        // Use all-years total for expenses
        summary.setTotalExpenses(expenseRepository.getTotalAllYears());
        summary.setCurrentBalance(summary.getTotalCollected().subtract(summary.getTotalExpenses()));
        summary.setCurrentYear(currentYear);

        // Calculate category-wise deposits
        Map<String, BigDecimal> categoryDeposits = new HashMap<>();
        Map<String, BigDecimal> categoryDepositsBn = new HashMap<>();
        List<VdfDeposit> allDeposits = depositRepository.findAll();
        for (VdfDeposit deposit : allDeposits) {
            if (deposit.getCategory() != null) {
                String categoryName = deposit.getCategory().getCategoryName();
                String categoryNameBn = deposit.getCategory().getCategoryNameBn() != null ?
                    deposit.getCategory().getCategoryNameBn() : deposit.getCategory().getCategoryName();
                categoryDeposits.put(categoryName,
                    categoryDeposits.getOrDefault(categoryName, BigDecimal.ZERO)
                        .add(deposit.getAmount()));
                categoryDepositsBn.put(categoryNameBn,
                    categoryDepositsBn.getOrDefault(categoryNameBn, BigDecimal.ZERO)
                        .add(deposit.getAmount()));
            }
        }
        summary.setCategoryWiseDeposits(categoryDeposits);
        summary.setCategoryWiseDepositsBn(categoryDepositsBn);

        // Calculate category-wise expenses across all years (EN and BN)
        Map<String, BigDecimal> categoryExpenses = new HashMap<>();
        Map<String, BigDecimal> categoryExpensesBn = new HashMap<>();
        List<Object[]> categoryTotals = expenseRepository.getCategoryTotalsAllYears();
        for (Object[] o : categoryTotals) {
            VdfExpenseCategory category = (VdfExpenseCategory) o[0];
            BigDecimal amount = (BigDecimal) o[1];
            String en = category.getCategoryName();
            String bn = category.getCategoryNameBn() != null ? category.getCategoryNameBn() : en;
            categoryExpenses.put(en, amount);
            categoryExpensesBn.put(bn, amount);
        }
        summary.setCategoryWiseExpenses(categoryExpenses);
        summary.setCategoryWiseExpensesBn(categoryExpensesBn);

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
        response.setSourceName(deposit.getSourceName());
        response.setYear(deposit.getYear());
        response.setNotes(deposit.getNotes());

        if (deposit.getMember() != null) {
            response.setMemberId(deposit.getMember().getId());
            response.setMemberName(com.dhuripara.util.NameUtil.buildMemberName(deposit.getMember()));
            
            // Also populate the full member object for frontend filtering
            MemberResponse memberResponse = new MemberResponse();
            memberResponse.setId(deposit.getMember().getId());
            memberResponse.setFirstName(deposit.getMember().getFirstName());
            memberResponse.setLastName(deposit.getMember().getLastName());
            memberResponse.setFirstNameBn(deposit.getMember().getFirstNameBn());
            memberResponse.setLastNameBn(deposit.getMember().getLastNameBn());
            memberResponse.setPhone(deposit.getMember().getPhone());
            memberResponse.setIsOperator(deposit.getMember().getIsOperator());
            memberResponse.setRole(deposit.getMember().getRole());
            memberResponse.setIsActive(deposit.getMember().getIsActive());
            response.setMember(memberResponse);
        }

        if (deposit.getCategory() != null) {
            response.setCategoryId(deposit.getCategory().getId());
            response.setCategoryName(deposit.getCategory().getCategoryName());
            // include bn category name if present
            response.setCategoryNameBn(deposit.getCategory().getCategoryNameBn());
        }
        // include bn source name
        response.setSourceNameBn(deposit.getSourceNameBn());

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
            response.setCategoryNameBn(expense.getCategory().getCategoryNameBn());
        }
        response.setDescription(expense.getDescription());
        response.setDescriptionBn(expense.getDescriptionBn());
        response.setNotes(expense.getNotes());
        return response;
    }

    private VdfFamilyConfigResponse convertFamilyToResponse(VdfFamilyConfig family, Integer year) {
        VdfFamilyConfigResponse response = new VdfFamilyConfigResponse();
        response.setId(family.getId());
        response.setMemberId(family.getMember().getId());
        response.setMemberName(com.dhuripara.util.NameUtil.buildMemberName(family.getMember()));
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

        // Also compute all-time totals (since effectiveFrom to today)
        java.math.BigDecimal paidAll = contributionRepository.getTotalByFamily(family.getId());
        if (paidAll == null) paidAll = java.math.BigDecimal.ZERO;

        // compute all-time required sum from effectiveFrom to today
        java.time.LocalDate start = family.getEffectiveFrom();
        java.time.LocalDate today = java.time.LocalDate.now();
        if (start == null) {
            if (family.getCreatedAt() != null) start = family.getCreatedAt().toLocalDate();
            else start = java.time.LocalDate.of(2023, 6, 1);
        }

        java.math.BigDecimal requiredSum = java.math.BigDecimal.ZERO;
        java.time.LocalDate iter = java.time.LocalDate.of(start.getYear(), start.getMonthValue(), 1);
        while (!iter.isAfter(today)) {
            String monText = String.format("%04d-%02d", iter.getYear(), iter.getMonthValue());
            if (!vdfFamilyExemptionRepository.existsByFamilyIdAndMonthYear(family.getId(), monText)) {
                java.math.BigDecimal req = vdfMonthlyConfigRepository.findByMonthYear(monText)
                        .map(VdfMonthlyConfig -> VdfMonthlyConfig.getRequiredAmount())
                        .orElse(family.getMonthlyAmount());
                requiredSum = requiredSum.add(req == null ? java.math.BigDecimal.ZERO : req);
            }
            iter = iter.plusMonths(1);
        }

        java.math.BigDecimal dueAll = requiredSum.subtract(paidAll == null ? java.math.BigDecimal.ZERO : paidAll);
        if (dueAll.compareTo(java.math.BigDecimal.ZERO) < 0) dueAll = java.math.BigDecimal.ZERO;

        response.setTotalPaidAllTime(paidAll);
        response.setTotalDueAllTime(dueAll);
        // alias fields for compatibility
        response.setTotalAmountPaid(paidAll);
        response.setTotalAmountDue(dueAll);

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
                .categoryNameBn(category.getCategoryNameBn())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .build();
    }

    public VdfExpenseResponse updateExpense(UUID id, VdfExpenseRequest request) {
        VdfExpense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAmount(request.getAmount());
        expense.setCategory(expenseCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
        expense.setDescription(request.getDescription());
        expense.setDescriptionBn(request.getDescriptionBn());
        expense.setNotes(request.getNotes());
        expense.setYear(request.getExpenseDate().getYear());
        expense.setMonth(request.getExpenseDate().getMonth().getValue());
        return convertExpenseToResponse(expenseRepository.save(expense));
    }

    public void deleteExpense(UUID id) {
        expenseRepository.deleteById(id);
    }
}