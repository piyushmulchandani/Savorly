package com.application.savorly.dto.search;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableSearchDto {

    @NotNull
    private Long restaurantId;
    private Integer tableNumber;
    private Boolean occupied;
    private Integer numPeople;

}
