package com.dhuripara.repository;

import com.dhuripara.model.VdfMonthlyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VdfMonthlyConfigRepository extends JpaRepository<VdfMonthlyConfig, UUID> {
    Optional<VdfMonthlyConfig> findByMonthYear(String monthYear);
    List<VdfMonthlyConfig> findByIsActiveTrueOrderByMonthYearDesc();
    boolean existsByMonthYear(String monthYear);
}
