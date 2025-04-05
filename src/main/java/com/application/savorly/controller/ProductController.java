package com.application.savorly.controller;

import com.application.savorly.dto.create.ProductCreationDto;
import com.application.savorly.dto.response.ProductResponseDto;
import com.application.savorly.facade.ProductFacade;
import lombok.extern.slf4j.Slf4j;
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
    public void addProduct(
            @PathVariable Long restaurantId,
            @RequestBody ProductCreationDto productCreationDto
    ) {
        productFacade.addProduct(restaurantId, productCreationDto);
    }

    @GetMapping("/{restaurantId}")
    public List<ProductResponseDto> getRestaurantProducts(
            @PathVariable Long restaurantId
    ) {
        return productFacade.getRestaurantProducts(restaurantId);
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(
            @PathVariable Long productId
    ) {
        productFacade.deleteProduct(productId);
    }
}
