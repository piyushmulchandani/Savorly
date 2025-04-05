package com.application.savorly.facade;

import com.application.savorly.config.interfaces.hasAnyRole;
import com.application.savorly.config.interfaces.hasRestaurantAdminRole;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.dto.create.ProductCreationDto;
import com.application.savorly.dto.response.ProductResponseDto;
import com.application.savorly.mapper.ProductMapper;
import com.application.savorly.service.ProductService;
import com.application.savorly.service.RestaurantService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductFacade {

    private final ProductService productService;
    private final RestaurantService restaurantService;
    private final ProductMapper productMapper;

    public ProductFacade(ProductService productService, RestaurantService restaurantService, ProductMapper productMapper) {
        this.productService = productService;
        this.restaurantService = restaurantService;
        this.productMapper = productMapper;
    }

    @hasRestaurantAdminRole
    public void addProduct(Long restaurantId, ProductCreationDto productCreationDto) {
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
        productService.createProduct(restaurant, productCreationDto);
    }

    @hasAnyRole
    public List<ProductResponseDto> getRestaurantProducts(Long restaurantId) {
        return productMapper.productToProductResponseDtoList(productService.getRestaurantProducts(restaurantId));
    }

    @hasRestaurantAdminRole
    public void deleteProduct(Long productId) {
        Product product = productService.findById(productId);
        productService.deleteProduct(product);
    }
}
