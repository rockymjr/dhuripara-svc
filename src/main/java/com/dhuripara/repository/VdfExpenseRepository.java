package com.dhuripara.repository;

import com.dhuripara.model.VdfExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface VdfExpenseRepository extends JpaRepository<VdfExpense, UUID> {
    List<VdfExpense> findAllByOrderByExpenseDateDesc();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM VdfExpense e")
    BigDecimal getTotalExpenses();
}