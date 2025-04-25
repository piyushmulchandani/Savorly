package com.application.savorly.dto.create;

import com.application.savorly.domain.catalog.CuisineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCreationDto {

    @NotBlank
    private String name;
    @NotNull
    private LocalTime openTime;
    @NotNull
    private LocalTime closeTime;
    @NotNull
    private CuisineType cuisineType;
    private String description;
    @NotNull
    private String address;
    private String phone;
    @NotNull
    private String city;
    @NotNull
    private String country;
}
