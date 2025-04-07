package com.application.savorly.controller;

import com.application.savorly.dto.create.ProductCreationDto;
import com.application.savorly.dto.response.ProductResponseDto;
import com.application.savorly.dto.search.ProductSearchDto;
import com.application.savorly.facade.ProductFacade;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductFacade productFacade;

    public ProductController(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    @PostMapping("/{restaurantId}")
    public ProductResponseDto addProduct(
            @PathVariable Long restaurantId,
            @RequestBody ProductCreationDto productCreationDto
    ) {
        return productFacade.addProduct(restaurantId, productCreationDto);
    }

    @GetMapping("/{restaurantId}")
    public List<ProductResponseDto> getRestaurantProductsFiltered(
            @PathVariable Long restaurantId,
            @ParameterObject @Valid ProductSearchDto productSearchDto
    ) {
        return productFacade.getRestaurantProductsFiltered(restaurantId, productSearchDto);
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(
            @PathVariable Long productId
    ) {
        productFacade.deleteProduct(productId);
    }
}
