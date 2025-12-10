package com.dhuripara.util;

import com.dhuripara.model.*;
import com.dhuripara.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Data migration utility to populate Bengali name/description columns
 * via approximate transliteration.
 * 
 * WARNING: This is approximate and may not be perfectly accurate.
 * Recommend manual review and crowdsourced corrections.
 * 
 * Usage: Inject this component and call migrate() to populate all BN columns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BengaliDataMigration {

    private final MemberRepository memberRepository;
    private final VdfDepositRepository vdfDepositRepository;
    private final VdfDepositCategoryRepository vdfDepositCategoryRepository;
    private final VdfExpenseRepository vdfExpenseRepository;
    private final VdfExpenseCategoryRepository vdfExpenseCategoryRepository;

    /**
     * Run all migrations to populate Bengali columns.
     * This is safe to call multiple times - it only updates null BN fields.
     */
    @Transactional
    public void migrate() {
        log.info("Starting Bengali transliteration data migration...");

        migrateMemberNames();
        migrateVdfDepositSourceNames();
        migrateVdfDepositCategoryNames();
        migrateVdfExpenseDescriptions();
        migrateVdfExpenseCategoryNames();

        log.info("Bengali transliteration migration completed.");
    }

    private void migrateMemberNames() {
        log.info("Migrating member names to Bengali...");
        List<Member> members = memberRepository.findAll();

        int updated = 0;
        for (Member member : members) {
            boolean changed = false;

            if (member.getFirstNameBn() == null && member.getFirstName() != null) {
                String bn = BengaliTransliterator.transliterate(member.getFirstName());
                if (bn != null && !bn.isBlank()) {
                    member.setFirstNameBn(bn);
                    changed = true;
                }
            }

            if (member.getLastNameBn() == null && member.getLastName() != null) {
                String bn = BengaliTransliterator.transliterate(member.getLastName());
                if (bn != null && !bn.isBlank()) {
                    member.setLastNameBn(bn);
                    changed = true;
                }
            }

            if (changed) {
                memberRepository.save(member);
                updated++;
            }
        }

        log.info("Migrated {} members", updated);
    }

    private void migrateVdfDepositSourceNames() {
        log.info("Migrating VDF deposit source names to Bengali...");
        List<VdfDeposit> deposits = vdfDepositRepository.findAll();

        int updated = 0;
        for (VdfDeposit deposit : deposits) {
            if (deposit.getSourceNameBn() == null && deposit.getSourceName() != null) {
                String bn = BengaliTransliterator.transliterate(deposit.getSourceName());
                if (bn != null && !bn.isBlank()) {
                    deposit.setSourceNameBn(bn);
                    vdfDepositRepository.save(deposit);
                    updated++;
                }
            }
        }

        log.info("Migrated {} VDF deposits", updated);
    }

    private void migrateVdfDepositCategoryNames() {
        log.info("Migrating VDF deposit category names to Bengali...");
        List<VdfDepositCategory> categories = vdfDepositCategoryRepository.findAll();

        int updated = 0;
        for (VdfDepositCategory category : categories) {
            if (category.getCategoryNameBn() == null && category.getCategoryName() != null) {
                String bn = BengaliTransliterator.transliterate(category.getCategoryName());
                if (bn != null && !bn.isBlank()) {
                    category.setCategoryNameBn(bn);
                    vdfDepositCategoryRepository.save(category);
                    updated++;
                }
            }
        }

        log.info("Migrated {} VDF deposit categories", updated);
    }

    private void migrateVdfExpenseDescriptions() {
        log.info("Migrating VDF expense descriptions to Bengali...");
        List<VdfExpense> expenses = vdfExpenseRepository.findAll();

        int updated = 0;
        for (VdfExpense expense : expenses) {
            if (expense.getDescriptionBn() == null && expense.getDescription() != null) {
                String bn = BengaliTransliterator.transliterate(expense.getDescription());
                if (bn != null && !bn.isBlank()) {
                    expense.setDescriptionBn(bn);
                    vdfExpenseRepository.save(expense);
                    updated++;
                }
            }
        }

        log.info("Migrated {} VDF expenses", updated);
    }

    private void migrateVdfExpenseCategoryNames() {
        log.info("Migrating VDF expense category names to Bengali...");
        List<VdfExpenseCategory> categories = vdfExpenseCategoryRepository.findAll();

        int updated = 0;
        for (VdfExpenseCategory category : categories) {
            if (category.getCategoryNameBn() == null && category.getCategoryName() != null) {
                String bn = BengaliTransliterator.transliterate(category.getCategoryName());
                if (bn != null && !bn.isBlank()) {
                    category.setCategoryNameBn(bn);
                    vdfExpenseCategoryRepository.save(category);
                    updated++;
                }
            }
        }

        log.info("Migrated {} VDF expense categories", updated);
    }
}
