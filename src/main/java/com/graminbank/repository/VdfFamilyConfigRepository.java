package com.graminbank.repository;

import com.graminbank.model.VdfFamilyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VdfFamilyConfigRepository extends JpaRepository<VdfFamilyConfig, UUID> {

    Optional<VdfFamilyConfig> findByMemberId(UUID memberId);

    List<VdfFamilyConfig> findByIsContributionEnabledTrue();

    @Query("SELECT v FROM VdfFamilyConfig v WHERE " +
            "LOWER(v.familyHeadName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.member.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.member.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<VdfFamilyConfig> searchFamilies(@Param("search") String search);

    Long countByIsContributionEnabledTrue();
}