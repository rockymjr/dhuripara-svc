package com.dhuripara.repository;

import com.dhuripara.model.FinancialYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialYearRepository extends JpaRepository<FinancialYear, UUID> {

    Optional<FinancialYear> findByYear(String year);

    Optional<FinancialYear> findByIsActiveTrue();
}