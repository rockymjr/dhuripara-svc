package com.dhuripara.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.UUID;

@Data
@Builder
public class VdfDepositCategoryResponse {
    private UUID id;
    private String categoryName;
    private String categoryNameBn;
    private String description;
    private Boolean isActive;
}
