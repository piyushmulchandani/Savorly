package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

@Service
public class RestaurantService {

    private RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public void save(Restaurant restaurant) {
        restaurantRepository.save(restaurant);
    }

    public Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found with id " + restaurantId));
    }
}
