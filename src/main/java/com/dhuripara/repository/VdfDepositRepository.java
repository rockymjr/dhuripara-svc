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

    List<VdfDeposit> findByYearOrderByDepositDateDesc(Integer year);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM VdfDeposit d WHERE d.year = :year")
    BigDecimal getTotalByYear(@Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM VdfDeposit d")
    BigDecimal getTotalDeposits();
}