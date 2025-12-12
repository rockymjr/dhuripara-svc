package com.dhuripara.repository;

import com.dhuripara.model.VdfNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VdfNotificationRepository extends JpaRepository<VdfNotification, UUID> {

    // Get all notifications for a specific member (including general notifications where member is null)
    @Query("SELECT n FROM VdfNotification n WHERE n.member.id = :memberId OR n.member IS NULL ORDER BY n.createdAt DESC")
    List<VdfNotification> findByMemberIdOrGeneral(@Param("memberId") UUID memberId);

    // Get unread notifications for a member
    @Query("SELECT n FROM VdfNotification n WHERE (n.member.id = :memberId OR n.member IS NULL) AND n.isRead = false ORDER BY n.createdAt DESC")
    List<VdfNotification> findUnreadByMemberIdOrGeneral(@Param("memberId") UUID memberId);

    // Mark all notifications as read for a member
    @Modifying
    @Query("UPDATE VdfNotification n SET n.isRead = true WHERE (n.member.id = :memberId OR n.member IS NULL) AND n.isRead = false")
    void markAllAsReadForMember(@Param("memberId") UUID memberId);

    // Delete a specific notification
    void deleteById(UUID id);

    // Get all general notifications (where member is null)
    List<VdfNotification> findByMemberIsNullOrderByCreatedAtDesc();
}

