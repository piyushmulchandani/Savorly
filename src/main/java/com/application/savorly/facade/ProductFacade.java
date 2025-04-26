package com.application.savorly.facade;

import com.application.savorly.config.exceptions.BadRequestException;
import com.application.savorly.config.interfaces.hasAnyRole;
import com.application.savorly.config.interfaces.hasRestaurantAdminRole;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.dto.create.ProductCreationDto;
import com.application.savorly.dto.response.ProductResponseDto;
import com.application.savorly.dto.search.ProductSearchDto;
import com.application.savorly.mapper.ProductMapper;
import com.application.savorly.service.ProductService;
import com.application.savorly.service.RestaurantService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ProductFacade {

    private final ProductService productService;
    private final RestaurantService restaurantService;
    private final ProductMapper productMapper;
    private final RestaurantFacade restaurantFacade;

    public ProductFacade(ProductService productService, RestaurantService restaurantService, ProductMapper productMapper, RestaurantFacade restaurantFacade) {
        this.productService = productService;
        this.restaurantService = restaurantService;
        this.productMapper = productMapper;
        this.restaurantFacade = restaurantFacade;
    }

    @hasRestaurantAdminRole
    public ProductResponseDto addProduct(ProductCreationDto productCreationDto) {
        Restaurant restaurant = restaurantService.getRestaurant(productCreationDto.getRestaurantId());
        restaurantFacade.checkRestaurantPermission(restaurant.getId());

        if (productService.productAlreadyExists(productCreationDto.getRestaurantId(), productCreationDto.getName())) {
            throw new BadRequestException("Product already exists");
        }
        return productMapper.productToProductResponseDto(productService.createProduct(restaurant, productCreationDto));
    }

    @hasAnyRole
    public List<ProductResponseDto> getRestaurantProductsFiltered(ProductSearchDto productSearchDto) {
        return productMapper.productToProductResponseDtoList(productService.getRestaurantProductsFiltered(productSearchDto));
    }

    @Transactional
    @hasRestaurantAdminRole
    public void deleteProduct(Long productId) {
        Product product = productService.findById(productId);
        restaurantFacade.checkRestaurantPermission(product.getRestaurant().getId());

        productService.deleteProduct(product);
    }
}
