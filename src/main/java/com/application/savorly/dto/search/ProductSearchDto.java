package com.application.savorly.dto.search;

import com.application.savorly.domain.catalog.ProductCategory;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchDto {

    @NotNull
    private Long restaurantId;
    private String name;
    private ProductCategory category;

}
