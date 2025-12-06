package com.dhuripara.repository;

import com.dhuripara.model.VdfFamilyExemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VdfFamilyExemptionRepository extends JpaRepository<VdfFamilyExemption, UUID> {
    Optional<VdfFamilyExemption> findByFamilyIdAndMonthYear(UUID familyId, String monthYear);
    List<VdfFamilyExemption> findByFamilyId(UUID familyId);
    List<VdfFamilyExemption> findByMonthYear(String monthYear);
    boolean existsByFamilyIdAndMonthYear(UUID familyId, String monthYear);
}