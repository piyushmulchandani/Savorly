package com.application.savorly.dto.search;

import com.application.savorly.domain.catalog.CuisineType;
import com.application.savorly.domain.catalog.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchDto {

    private String name;
    private RestaurantStatus status;
    private CuisineType cuisineType;
    private String city;


    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateTime;
    private Integer numPeople;
}
