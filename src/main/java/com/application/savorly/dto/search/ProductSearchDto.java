package com.application.savorly.dto.search;

import com.application.savorly.domain.catalog.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchDto {

    private String name;
    private ProductCategory category;

}
