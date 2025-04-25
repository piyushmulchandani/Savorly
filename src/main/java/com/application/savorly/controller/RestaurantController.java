package com.application.savorly.controller;

import com.application.savorly.dto.create.RestaurantCreationDto;
import com.application.savorly.dto.modify.RestaurantModificationDto;
import com.application.savorly.dto.response.RestaurantResponseDto;
import com.application.savorly.dto.search.RestaurantSearchDto;
import com.application.savorly.facade.RestaurantFacade;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    private final RestaurantFacade restaurantFacade;

    public RestaurantController(RestaurantFacade restaurantFacade) {
        this.restaurantFacade = restaurantFacade;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RestaurantResponseDto createRestaurant(
            @RequestPart("restaurant") RestaurantCreationDto restaurantCreationDto,
            @RequestPart("file") MultipartFile file
    ) {
        return restaurantFacade.createRestaurant(restaurantCreationDto, file);
    }

    @PostMapping("/upload-image/{restaurantId}")
    public void uploadImage(
            @PathVariable Long restaurantId,
            @RequestParam("file") MultipartFile file
    ) {
        restaurantFacade.uploadImage(restaurantId, file);
    }

    @PostMapping("/accept/{restaurantId}")
    public void acceptRestaurantCreation(
            @PathVariable Long restaurantId
    ) {
        restaurantFacade.acceptRestaurant(restaurantId);
    }

    @PostMapping("/reject/{restaurantId}")
    public void rejectRestaurantCreation(
            @PathVariable Long restaurantId,
            @RequestBody String reason
    ) {
        restaurantFacade.rejectRestaurant(restaurantId, reason);
    }

    @GetMapping
    public List<RestaurantResponseDto> getRestaurants(
            @ParameterObject RestaurantSearchDto restaurantSearchDto
    ) {
        return restaurantFacade.getRestaurants(restaurantSearchDto);
    }

    @PatchMapping("/{restaurantId}")
    public RestaurantResponseDto updateRestaurant(
            @PathVariable Long restaurantId,
            @RequestBody RestaurantModificationDto restaurantModificationDto
    ) {
        return restaurantFacade.updateRestaurant(restaurantId, restaurantModificationDto);
    }

    @DeleteMapping("/{restaurantId}")
    public void deleteRestaurant(
            @PathVariable Long restaurantId
    ) {
        restaurantFacade.deleteRestaurant(restaurantId);
    }
}
