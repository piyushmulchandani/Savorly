package com.application.savorly.mapper;

import com.application.savorly.domain.entity.Product;
import com.application.savorly.dto.response.ProductResponseDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponseDto productToProductResponseDto(Product product);

    List<ProductResponseDto> productToProductResponseDtoList(List<Product> products);
}
