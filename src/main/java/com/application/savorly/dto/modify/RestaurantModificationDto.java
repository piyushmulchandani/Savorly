package com.application.savorly.dto.modify;

import com.application.savorly.domain.catalog.CuisineType;
import com.application.savorly.domain.catalog.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantModificationDto {

    private RestaurantStatus status;
    private LocalTime openTime;
    private LocalTime closeTime;
    private CuisineType cuisineType;
    private String description;
    private String address;
    private String phone;
    private String city;
    private String country;
    private String rejectionMessage;

}
