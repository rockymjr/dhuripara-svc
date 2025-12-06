package com.dhuripara.repository;

import com.dhuripara.model.VdfDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface VdfDepositRepository extends JpaRepository<VdfDeposit, UUID> {

    List<VdfDeposit> findAllByOrderByDepositDateDesc();

    @Query("SELECT d FROM VdfDeposit d WHERE YEAR(d.depositDate) = :year ORDER BY d.depositDate DESC")
    List<VdfDeposit> findByYear(@Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM VdfDeposit d")
    BigDecimal getTotalDeposits();

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM VdfDeposit d WHERE YEAR(d.depositDate) = :year")
    BigDecimal getTotalDepositsByYear(@Param("year") Integer year);
}