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
@RequestMapping("/api/v1/restaurants/products")
public class ProductController {

    private final ProductFacade productFacade;

    public ProductController(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    @PostMapping
    public ProductResponseDto addProduct(
            @RequestBody ProductCreationDto productCreationDto
    ) {
        return productFacade.addProduct(productCreationDto);
    }

    @GetMapping
    public List<ProductResponseDto> getRestaurantProductsFiltered(
            @ParameterObject @Valid ProductSearchDto productSearchDto
    ) {
        return productFacade.getRestaurantProductsFiltered(productSearchDto);
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(
            @PathVariable Long productId
    ) {
        productFacade.deleteProduct(productId);
    }
}
