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

    List<VdfExpense> findAllByOrderByExpenseDateDesc();

    @Query("SELECT e FROM VdfExpense e WHERE YEAR(e.expenseDate) = :year ORDER BY e.expenseDate DESC")
    List<VdfExpense> findByYear(@Param("year") Integer year);

    List<VdfExpense> findByCategoryOrderByExpenseDateDesc(String category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM VdfExpense e")
    BigDecimal getTotalExpenses();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM VdfExpense e WHERE YEAR(e.expenseDate) = :year")
    BigDecimal getTotalExpensesByYear(@Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM VdfExpense e WHERE e.category = :category")
    BigDecimal getTotalExpensesByCategory(@Param("category") String category);
}