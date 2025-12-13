package com.dhuripara.repository;

import com.dhuripara.model.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, UUID> {
    Optional<DocumentCategory> findByCategoryName(String categoryName);
    List<DocumentCategory> findByIsActiveTrueOrderByCategoryNameAsc();
}

