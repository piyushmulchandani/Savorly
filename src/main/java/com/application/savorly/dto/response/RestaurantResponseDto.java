package com.application.savorly.dto.response;

import com.application.savorly.domain.catalog.CuisineType;
import com.application.savorly.domain.catalog.RestaurantStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantResponseDto {

    private Long id;
    private String name;
    private RestaurantStatus status;
    private LocalTime openTime;
    private LocalTime closeTime;
    private CuisineType cuisineType;
    private String description;
    private String address;
    private String phone;
    private String city;
    private String country;
    private String imageUrl;
    private String ownershipProofUrl;
    private String rejectionMessage;

}
