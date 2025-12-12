package com.dhuripara.service;

import com.dhuripara.dto.response.VdfNotificationResponse;
import com.dhuripara.exception.ResourceNotFoundException;
import com.dhuripara.model.Member;
import com.dhuripara.model.VdfNotification;
import com.dhuripara.repository.MemberRepository;
import com.dhuripara.repository.VdfNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VdfNotificationService {

    private final VdfNotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    public List<VdfNotificationResponse> getNotificationsForMember(UUID memberId) {
        log.info("Fetching notifications for member: {}", memberId);
        List<VdfNotification> notifications = notificationRepository.findByMemberIdOrGeneral(memberId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<VdfNotificationResponse> getUnreadNotificationsForMember(UUID memberId) {
        log.info("Fetching unread notifications for member: {}", memberId);
        List<VdfNotification> notifications = notificationRepository.findUnreadByMemberIdOrGeneral(memberId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID memberId) {
        log.info("Marking notification {} as read for member {}", notificationId, memberId);
        VdfNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        // Verify the notification belongs to this member or is general
        if (notification.getMember() != null && !notification.getMember().getId().equals(memberId)) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID memberId) {
        log.info("Marking all notifications as read for member: {}", memberId);
        notificationRepository.markAllAsReadForMember(memberId);
    }

    @Transactional
    public void deleteNotification(UUID notificationId, UUID memberId) {
        log.info("Deleting notification {} for member {}", notificationId, memberId);
        VdfNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        // Verify the notification belongs to this member or is general
        if (notification.getMember() != null && !notification.getMember().getId().equals(memberId)) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public void createNotificationForAllMembers(String title, String titleBn, String message, String messageBn, String type, UUID relatedId) {
        log.info("Creating notification for all members: {}", title);
        VdfNotification notification = new VdfNotification();
        notification.setMember(null); // null means for all members
        notification.setTitle(title);
        notification.setTitleBn(titleBn);
        notification.setMessage(message);
        notification.setMessageBn(messageBn);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotificationForMember(UUID memberId, String title, String titleBn, String message, String messageBn, String type, UUID relatedId) {
        log.info("Creating notification for member {}: {}", memberId, title);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        VdfNotification notification = new VdfNotification();
        notification.setMember(member);
        notification.setTitle(title);
        notification.setTitleBn(titleBn);
        notification.setMessage(message);
        notification.setMessageBn(messageBn);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

    private VdfNotificationResponse convertToResponse(VdfNotification notification) {
        VdfNotificationResponse response = new VdfNotificationResponse();
        response.setId(notification.getId());
        if (notification.getMember() != null) {
            response.setMemberId(notification.getMember().getId());
            response.setMemberName(com.dhuripara.util.NameUtil.buildMemberName(notification.getMember()));
        }
        response.setTitle(notification.getTitle());
        response.setTitleBn(notification.getTitleBn());
        response.setMessage(notification.getMessage());
        response.setMessageBn(notification.getMessageBn());
        response.setType(notification.getType());
        response.setRelatedId(notification.getRelatedId());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}

