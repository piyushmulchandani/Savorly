package com.application.savorly.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableResponseDto {

    private Long id;
    private Integer tableNumber;
    private Boolean occupied;
    private BigDecimal currentCost;
    private Integer minPeople;
    private Integer maxPeople;
    private List<OrderResponseDto> orders;

}
