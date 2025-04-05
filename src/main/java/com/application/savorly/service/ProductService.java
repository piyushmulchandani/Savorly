package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.dto.create.ProductCreationDto;
import com.application.savorly.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public void createProduct(Restaurant restaurant, ProductCreationDto productCreationDto) {
        Product product = Product.builder()
                .name(productCreationDto.getName())
                .price(productCreationDto.getPrice())
                .category(productCreationDto.getCategory())
                .restaurant(restaurant).build();

        productRepository.save(product);
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id " + productId));
    }

    public List<Product> getRestaurantProducts(Long restaurantId) {
        return productRepository.findByRestaurant_Id(restaurantId);
    }

    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }
}
