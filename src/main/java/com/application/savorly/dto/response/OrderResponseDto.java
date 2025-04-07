package com.application.savorly.dto.response;

import com.application.savorly.domain.catalog.OrderType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponseDto {

    private Long id;
    private LocalDateTime orderTime;
    private OrderType type;
    private Boolean completed;
    private int tableNumber;
    private List<ProductResponseDto> products;

}
