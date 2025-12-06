package com.dhuripara.repository;

import com.dhuripara.model.VdfFamilyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VdfFamilyConfigRepository extends JpaRepository<VdfFamilyConfig, UUID> {
    List<VdfFamilyConfig> findAllByOrderByFamilyHeadNameAsc();
    Optional<VdfFamilyConfig> findByMemberId(UUID memberId);
    boolean existsByMemberId(UUID memberId);
    List<VdfFamilyConfig> findByIsContributionEnabledTrue();
}