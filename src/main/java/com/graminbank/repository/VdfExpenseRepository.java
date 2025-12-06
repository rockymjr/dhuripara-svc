package com.graminbank.repository;

import com.graminbank.model.VdfExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface VdfExpenseRepository extends JpaRepository<VdfExpense, UUID> {

    Page<VdfExpense> findAllByOrderByExpenseDateDesc(Pageable pageable);

    List<VdfExpense> findByCategoryIdOrderByExpenseDateDesc(UUID categoryId);

    @Query("SELECT e FROM VdfExpense e WHERE " +
            "EXTRACT(MONTH FROM e.expenseDate) = :month AND " +
            "EXTRACT(YEAR FROM e.expenseDate) = :year " +
            "ORDER BY e.expenseDate DESC")
    List<VdfExpense> findByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM VdfExpense e WHERE " +
            "EXTRACT(MONTH FROM e.expenseDate) = :month AND " +
            "EXTRACT(YEAR FROM e.expenseDate) = :year")
    BigDecimal getTotalExpensesForMonth(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM VdfExpense e WHERE " +
            "EXTRACT(YEAR FROM e.expenseDate) = :year")
    BigDecimal getTotalExpensesForYear(@Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM VdfExpense e")
    BigDecimal getTotalExpensesAllTime();

    @Query("SELECT e FROM VdfExpense e WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
            "ORDER BY e.expenseDate DESC")
    List<VdfExpense> findByDateRange(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
}