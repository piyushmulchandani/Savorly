package com.application.savorly.dto.create;

import com.application.savorly.domain.catalog.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreationDto {
    @NotNull
    private Long restaurantId;
    @NotBlank
    private String name;
    @NotNull
    private ProductCategory category;
    @NotNull
    private BigDecimal price;
}
