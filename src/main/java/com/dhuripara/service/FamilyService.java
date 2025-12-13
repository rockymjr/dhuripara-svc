package com.dhuripara.service;

import com.dhuripara.dto.response.FamilyDetailsResponse;
import com.dhuripara.model.Member;
import com.dhuripara.model.VdfFamilyConfig;
import com.dhuripara.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyService {

    private final MemberRepository memberRepository;
    private final VdfFamilyConfigRepository familyConfigRepository;
    private final DepositRepository depositRepository;
    private final LoanRepository loanRepository;
    private final VdfContributionRepository contributionRepository;

    public FamilyDetailsResponse getFamilyDetails(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (member.getFamilyId() == null) {
            throw new RuntimeException("Member is not associated with any family");
        }

        VdfFamilyConfig family = familyConfigRepository.findById(member.getFamilyId())
                .orElseThrow(() -> new RuntimeException("Family not found"));

        // Get all family members
        List<Member> familyMembers = memberRepository.findByFamilyIdAndIsActiveTrue(member.getFamilyId());

        // Build family member info
        List<FamilyDetailsResponse.FamilyMemberInfo> memberInfos = familyMembers.stream()
                .map(m -> {
                    BigDecimal memberDeposits = depositRepository.findByMemberId(m.getId()).stream()
                            .map(d -> d.getAmount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal memberLoans = loanRepository.findByMemberId(m.getId()).stream()
                            .map(l -> l.getLoanAmount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return FamilyDetailsResponse.FamilyMemberInfo.builder()
                            .id(m.getId())
                            .firstName(m.getFirstName())
                            .lastName(m.getLastName())
                            .phone(m.getPhone())
                            .dateOfBirth(m.getDateOfBirth())
                            .aadharNo(m.getAadharNo())
                            .voterNo(m.getVoterNo())
                            .panNo(m.getPanNo())
                            .totalDeposits(memberDeposits)
                            .totalLoans(memberLoans)
                            .build();
                })
                .collect(Collectors.toList());

        // Calculate family totals
        BigDecimal totalDeposits = familyMembers.stream()
                .flatMap(m -> depositRepository.findByMemberId(m.getId()).stream())
                .map(d -> d.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLoans = familyMembers.stream()
                .flatMap(m -> loanRepository.findByMemberId(m.getId()).stream())
                .map(l -> l.getLoanAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // VDF totals
        BigDecimal totalVdfContributions = contributionRepository.getTotalByFamily(family.getId());
        if (totalVdfContributions == null) {
            totalVdfContributions = BigDecimal.ZERO;
        }

        // VDF expenses are general, not family-specific, so we'll show all-time total
        // In future, if expenses need to be family-specific, we can add that
        BigDecimal totalVdfExpenses = BigDecimal.ZERO; // Family expenses not tracked separately

        return FamilyDetailsResponse.builder()
                .familyId(family.getId())
                .familyHeadName(family.getFamilyHeadName())
                .isContributionEnabled(family.getIsContributionEnabled())
                .effectiveFrom(family.getEffectiveFrom())
                .monthlyAmount(family.getMonthlyAmount())
                .notes(family.getNotes())
                .members(memberInfos)
                .totalDeposits(totalDeposits)
                .totalLoans(totalLoans)
                .totalVdfContributions(totalVdfContributions)
                .totalVdfExpenses(totalVdfExpenses)
                .build();
    }
}

