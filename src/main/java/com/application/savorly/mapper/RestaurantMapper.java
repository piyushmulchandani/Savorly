package com.application.savorly.mapper;

import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.dto.response.RestaurantResponseDto;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface RestaurantMapper {
    RestaurantResponseDto restaurantToRestaurantResponseDto(Restaurant restaurant);

    List<RestaurantResponseDto> restaurantListToRestaurantResponseDtoList(List<Restaurant> restaurantList);
}
