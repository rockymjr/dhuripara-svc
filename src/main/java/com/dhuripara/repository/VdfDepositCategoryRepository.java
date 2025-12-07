package com.dhuripara.repository;

import com.dhuripara.model.VdfDepositCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VdfDepositCategoryRepository extends JpaRepository<VdfDepositCategory, UUID> {
    List<VdfDepositCategory> findByIsActiveTrueOrderByCategoryNameAsc();
}
