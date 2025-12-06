package com.dhuripara.repository;

import com.dhuripara.model.VdfFamilyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VdfFamilyConfigRepository extends JpaRepository<VdfFamilyConfig, UUID> {
    Optional<VdfFamilyConfig> findByMemberId(UUID memberId);

    List<VdfFamilyConfig> findByIsContributionEnabledTrue();

    @Query("SELECT COUNT(f) FROM VdfFamilyConfig f WHERE f.isContributionEnabled = true")
    Long countActiveContributors();
}