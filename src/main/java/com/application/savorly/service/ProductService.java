package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.QProduct;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.dto.create.ProductCreationDto;
import com.application.savorly.dto.search.ProductSearchDto;
import com.application.savorly.repository.ProductRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public boolean productAlreadyExists(Long restaurantId, String productName) {
        return productRepository.existsByRestaurant_IdAndName(restaurantId, productName);
    }

    public Product createProduct(Restaurant restaurant, ProductCreationDto productCreationDto) {
        Product product = Product.builder()
                .name(productCreationDto.getName())
                .price(productCreationDto.getPrice())
                .category(productCreationDto.getCategory())
                .build();

        restaurant.addProduct(product);

        return productRepository.save(product);
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id " + productId));
    }

    public List<Product> getRestaurantProductsFiltered(ProductSearchDto productSearchDto) {
        Predicate predicate = getWhere(productSearchDto);
        return (List<Product>) productRepository.findAll(predicate);
    }

    public void deleteProduct(Product product) {
        product.getRestaurant().getProducts().remove(product);
        productRepository.delete(product);
    }

    private Predicate getWhere(ProductSearchDto productSearchDto) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(QProduct.product.restaurant.id.eq(productSearchDto.getRestaurantId()));
        if (productSearchDto.getCategory() != null) {
            where.and(QProduct.product.category.eq(productSearchDto.getCategory()));
        }
        if (productSearchDto.getName() != null) {
            where.and(QProduct.product.name.containsIgnoreCase(productSearchDto.getName()));
        }

        return where;
    }
}
