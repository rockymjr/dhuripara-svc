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
    List<VdfContribution> findByFamilyIdOrderByPaymentDateDesc(UUID familyId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM VdfContribution c")
    BigDecimal getTotalContributions();

    // Get paid amount for a specific family and month
    @Query(value = "SELECT COALESCE(SUM((alloc->>'amount')::decimal), 0) " +
            "FROM vdf_contributions c, jsonb_array_elements(c.month_allocations) as alloc " +
            "WHERE c.family_id = :familyId AND alloc->>'month' = :monthYear",
            nativeQuery = true)
    BigDecimal getPaidAmountForFamilyMonth(@Param("familyId") UUID familyId,
                                           @Param("monthYear") String monthYear);
}
