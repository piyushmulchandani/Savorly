package com.application.savorly.facade;

import com.application.savorly.mapper.ProductMapper;
import com.application.savorly.service.ProductService;
import org.springframework.stereotype.Component;

@Component
public class ProductFacade {

    private ProductService productService;
    private ProductMapper productMapper;

    public ProductFacade(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }
}
