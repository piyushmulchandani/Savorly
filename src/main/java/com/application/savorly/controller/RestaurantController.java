package com.application.savorly.controller;

import com.application.savorly.facade.RestaurantFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    private RestaurantFacade restaurantFacade;

    public RestaurantController(RestaurantFacade restaurantFacade) {
        this.restaurantFacade = restaurantFacade;
    }

    @PostMapping("/{restaurantId}/upload")
    public void uploadImage(
            @PathVariable Long restaurantId,
            @RequestParam("file") MultipartFile file
    ) {
        restaurantFacade.uploadImage(restaurantId, file);
    }
}
