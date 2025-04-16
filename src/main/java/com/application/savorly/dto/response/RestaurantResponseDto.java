package com.application.savorly.dto.response;

import com.application.savorly.domain.catalog.CuisineType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantResponseDto {

    private Long id;
    private String name;
    private CuisineType cuisineType;
    private String description;
    private String address;
    private String phone;
    private String city;
    private String country;

}
