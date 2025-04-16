package com.application.savorly.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableCreationDto {

    @NotNull
    private Long restaurantId;
    @NotNull
    private Integer minPeople;
    @NotNull
    private Integer maxPeople;

}
