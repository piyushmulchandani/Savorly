package com.application.savorly.dto.search;

import com.application.savorly.domain.catalog.OrderType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchDto {

    @NotNull
    private Long restaurantId;
    private OrderType orderType;
    private Boolean completed;
    private Long tableId;

}
