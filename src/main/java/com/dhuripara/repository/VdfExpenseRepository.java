package com.dhuripara.repository;

import com.dhuripara.model.VdfExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface VdfExpenseRepository extends JpaRepository<VdfExpense, UUID> {

    List<VdfExpense> findByYearOrderByExpenseDateDesc(Integer year);

    List<VdfExpense> findByYearAndMonthOrderByExpenseDateDesc(Integer year, Integer month);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM VdfExpense e WHERE e.year = :year")
    BigDecimal getTotalByYear(@Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM VdfExpense e WHERE e.year = :year AND e.month = :month")
    BigDecimal getTotalByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT e.category, SUM(e.amount) FROM VdfExpense e WHERE e.year = :year GROUP BY e.category")
    List<Object[]> getCategoryTotalsByYear(@Param("year") Integer year);
}