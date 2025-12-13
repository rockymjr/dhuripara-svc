package com.dhuripara.repository;

import com.dhuripara.model.MemberDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemberDocumentRepository extends JpaRepository<MemberDocument, UUID> {
    List<MemberDocument> findByMemberIdOrderByUploadedAtDesc(UUID memberId);
    
    List<MemberDocument> findByMemberIdAndDocumentCategoryIdOrderByUploadedAtDesc(UUID memberId, UUID categoryId);
    
    @Query("SELECT md FROM MemberDocument md WHERE md.member.familyId = :familyId ORDER BY md.member.firstName, md.member.lastName, md.uploadedAt DESC")
    List<MemberDocument> findByFamilyId(@Param("familyId") UUID familyId);
    
    @Query("SELECT md FROM MemberDocument md WHERE md.member.familyId = :familyId AND md.member.familyId IS NOT NULL ORDER BY md.member.firstName, md.member.lastName, md.uploadedAt DESC")
    List<MemberDocument> findByMemberFamilyId(@Param("familyId") UUID familyId);
}

