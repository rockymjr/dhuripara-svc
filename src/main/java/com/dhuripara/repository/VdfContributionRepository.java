package com.dhuripara.repository;

import com.dhuripara.model.VdfContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface VdfContributionRepository extends JpaRepository<VdfContribution, UUID> {

    List<VdfContribution> findByFamilyConfigIdOrderByYearDescMonthDesc(UUID familyConfigId);

    List<VdfContribution> findByYearOrderByMonthAsc(Integer year);

    Optional<VdfContribution> findByFamilyConfigIdAndMonthAndYear(UUID familyConfigId, Integer month, Integer year);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM VdfContribution c WHERE c.year = :year")
    BigDecimal getTotalContributionsByYear(@Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM VdfContribution c WHERE c.year = :year AND c.month = :month")
    BigDecimal getTotalContributionsByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM VdfContribution c")
    BigDecimal getTotalContributions();
}