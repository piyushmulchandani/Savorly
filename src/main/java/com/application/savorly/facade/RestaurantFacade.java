package com.application.savorly.facade;

import com.application.savorly.config.exceptions.BadRequestException;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.mapper.RestaurantMapper;
import com.application.savorly.service.CloudinaryService;
import com.application.savorly.service.RestaurantService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class RestaurantFacade {

    private RestaurantService restaurantService;
    private RestaurantMapper restaurantMapper;
    private CloudinaryService cloudinaryService;

    public RestaurantFacade(RestaurantService restaurantService, RestaurantMapper restaurantMapper, CloudinaryService cloudinaryService) {
        this.restaurantService = restaurantService;
        this.restaurantMapper = restaurantMapper;
        this.cloudinaryService = cloudinaryService;
    }

    public void uploadImage(Long restaurantId, MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
            restaurant.setImageUrl(imageUrl);
            restaurantService.save(restaurant);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
