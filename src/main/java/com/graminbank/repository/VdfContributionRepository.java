package com.graminbank.repository;

import com.graminbank.model.VdfContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VdfContributionRepository extends JpaRepository<VdfContribution, UUID> {

    List<VdfContribution> findByFamilyConfigIdAndPaymentYear(UUID familyConfigId, Integer year);

    Optional<VdfContribution> findByFamilyConfigIdAndPaymentMonthAndPaymentYear(
            UUID familyConfigId, Integer month, Integer year);

    List<VdfContribution> findByPaymentMonthAndPaymentYear(Integer month, Integer year);

    @Query("SELECT COALESCE(SUM(c.amountPaid), 0) FROM VdfContribution c " +
            "WHERE c.paymentMonth = :month AND c.paymentYear = :year")
    BigDecimal getTotalCollectedForMonth(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(c.amountPaid), 0) FROM VdfContribution c " +
            "WHERE c.paymentYear = :year")
    BigDecimal getTotalCollectedForYear(@Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(c.amountPaid), 0) FROM VdfContribution c")
    BigDecimal getTotalCollectedAllTime();
}