package com.application.savorly.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableCreationDto {

    private Long restaurantId;
    private Integer minPeople;
    private Integer maxPeople;

}
