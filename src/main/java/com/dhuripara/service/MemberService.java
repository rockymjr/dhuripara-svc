package com.dhuripara.service;

import com.dhuripara.dto.request.ChangePinRequest;
import com.dhuripara.dto.request.MemberRequest;
import com.dhuripara.dto.response.MemberResponse;
import com.dhuripara.exception.BusinessException;
import com.dhuripara.exception.ResourceNotFoundException;
import com.dhuripara.model.Member;
import com.dhuripara.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        log.info("Creating new member: {} {}", request.getFirstName(), request.getLastName());

        Member member = new Member();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setFirstNameBn(request.getFirstNameBn());
        member.setLastNameBn(request.getLastNameBn());
        member.setPhone(request.getPhone());
        member.setPin(request.getPin());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setAadharNo(request.getAadharNo());
        member.setVoterNo(request.getVoterNo());
        member.setPanNo(request.getPanNo());
        member.setFamilyId(request.getFamilyId());
        member.setJoiningDate(LocalDate.now());
        member.setIsActive(true);
        member.setIsBlocked(false);
        member.setFailedLoginAttempts(0);
        // set role, default to MEMBER
        member.setRole(request.getRole() != null ? request.getRole() : "MEMBER");
        // ensure isOperator boolean is consistent with role
        member.setIsOperator("OPERATOR".equalsIgnoreCase(member.getRole()));

        Member savedMember = memberRepository.save(member);
        return convertToResponse(savedMember);
    }

    public List<MemberResponse> getAllActiveMembers() {
        return memberRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .sorted((a, b) -> {
                    String nameA = (a.getFirstName() != null ? a.getFirstName() : "") + " " + (a.getLastName() != null ? a.getLastName() : "");
                    String nameB = (b.getFirstName() != null ? b.getFirstName() : "") + " " + (b.getLastName() != null ? b.getLastName() : "");
                    return nameA.trim().compareToIgnoreCase(nameB.trim());
                })
                .collect(Collectors.toList());
    }

    public List<MemberResponse> searchMembers(String search) {
        return memberRepository.searchMembers(search)
                .stream()
                .map(this::convertToResponse)
                .sorted((a, b) -> {
                    String nameA = (a.getFirstName() != null ? a.getFirstName() : "") + " " + (a.getLastName() != null ? a.getLastName() : "");
                    String nameB = (b.getFirstName() != null ? b.getFirstName() : "") + " " + (b.getLastName() != null ? b.getLastName() : "");
                    return nameA.trim().compareToIgnoreCase(nameB.trim());
                })
                .collect(Collectors.toList());
    }

    public MemberResponse getMemberById(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return convertToResponse(member);
    }

    @Transactional
    public MemberResponse updateMember(UUID id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setFirstNameBn(request.getFirstNameBn());
        member.setLastNameBn(request.getLastNameBn());
        member.setPhone(request.getPhone());
        member.setPin(request.getPin());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setAadharNo(request.getAadharNo());
        member.setVoterNo(request.getVoterNo());
        member.setPanNo(request.getPanNo());
        member.setFamilyId(request.getFamilyId());
        member.setIsOperator(request.getIsOperator());
        // if role is provided, override isOperator to match role
        if (request.getRole() != null) {
            member.setRole(request.getRole());
            member.setIsOperator("OPERATOR".equalsIgnoreCase(request.getRole()));
        }

        Member updatedMember = memberRepository.save(member);
        return convertToResponse(updatedMember);
    }

    @Transactional
    public void deactivateMember(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        member.setIsActive(false);
        memberRepository.save(member);
    }

    @Transactional
    public void unblockMember(UUID id) {
        log.info("Unblocking member: {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setIsBlocked(false);
        member.setBlockedUntil(null);
        member.setFailedLoginAttempts(0);
        member.setLastFailedLogin(null);

        memberRepository.save(member);
        log.info("Member {} unblocked successfully", id);
    }

    @Transactional
    public void changePin(UUID memberId, ChangePinRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!request.getOldPin().equals(member.getPin())) {
            throw new BusinessException("Old PIN is incorrect");
        }

        member.setPin(request.getNewPin());
        memberRepository.save(member);
    }

    private MemberResponse convertToResponse(Member member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setFirstNameBn(member.getFirstNameBn());
        response.setLastNameBn(member.getLastNameBn());
        response.setPhone(member.getPhone());
        response.setJoiningDate(member.getJoiningDate());
        response.setIsActive(member.getIsActive());
        response.setIsOperator(member.getIsOperator());
        response.setRole(member.getRole());
        response.setPin(member.getPin());
        response.setIsBlocked(member.isCurrentlyBlocked());
        response.setBlockedUntil(member.getBlockedUntil());
        response.setFailedLoginAttempts(member.getFailedLoginAttempts());
        response.setDateOfBirth(member.getDateOfBirth());
        response.setAadharNo(member.getAadharNo());
        response.setVoterNo(member.getVoterNo());
        response.setPanNo(member.getPanNo());
        response.setFamilyId(member.getFamilyId());
        return response;
    }
}