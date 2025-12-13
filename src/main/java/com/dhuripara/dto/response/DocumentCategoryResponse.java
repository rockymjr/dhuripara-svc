package com.dhuripara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCategoryResponse {
    private UUID id;
    private String categoryName;
    private String description;
    private Boolean isActive;
}

