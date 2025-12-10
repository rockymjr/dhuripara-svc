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
        member.setJoiningDate(LocalDate.now());
        member.setIsActive(true);
        member.setIsBlocked(false);
        member.setFailedLoginAttempts(0);

        Member savedMember = memberRepository.save(member);
        return convertToResponse(savedMember);
    }

    public List<MemberResponse> getAllActiveMembers() {
        return memberRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<MemberResponse> searchMembers(String search) {
        return memberRepository.searchMembers(search)
                .stream()
                .map(this::convertToResponse)
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
        member.setIsOperator(request.getIsOperator());

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
        response.setPin(member.getPin());
        response.setIsBlocked(member.isCurrentlyBlocked());
        response.setBlockedUntil(member.getBlockedUntil());
        response.setFailedLoginAttempts(member.getFailedLoginAttempts());
        return response;
    }
}