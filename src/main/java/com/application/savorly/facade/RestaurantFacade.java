package com.application.savorly.facade;

import com.application.savorly.config.exceptions.BadRequestException;
import com.application.savorly.config.exceptions.ForbiddenException;
import com.application.savorly.config.interfaces.hasAdminRole;
import com.application.savorly.config.interfaces.hasAnyRole;
import com.application.savorly.config.interfaces.hasRestaurantAdminRole;
import com.application.savorly.domain.catalog.RestaurantStatus;
import com.application.savorly.domain.catalog.SavorlyRole;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.dto.create.RestaurantCreationDto;
import com.application.savorly.dto.create.TableCreationDto;
import com.application.savorly.dto.modify.RestaurantModificationDto;
import com.application.savorly.dto.response.RestaurantResponseDto;
import com.application.savorly.dto.search.RestaurantSearchDto;
import com.application.savorly.mapper.RestaurantMapper;
import com.application.savorly.service.CloudinaryService;
import com.application.savorly.service.RestaurantService;
import com.application.savorly.service.TableService;
import com.application.savorly.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class RestaurantFacade {

    private final RestaurantService restaurantService;
    private final RestaurantMapper restaurantMapper;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final TableService tableService;

    public RestaurantFacade(RestaurantService restaurantService, RestaurantMapper restaurantMapper, CloudinaryService cloudinaryService, UserService userService, TableService tableService) {
        this.restaurantService = restaurantService;
        this.restaurantMapper = restaurantMapper;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
        this.tableService = tableService;
    }

    @hasAnyRole
    public RestaurantResponseDto createRestaurant(RestaurantCreationDto restaurantCreationDto, MultipartFile file) {
        UserDetails creator = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SavorlyUser restaurantAdmin = userService.findUserByUsername(creator.getUsername());

        String pdfUrl;
        try {
            pdfUrl = cloudinaryService.uploadPdf(file, "RestaurantDocument");
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }

        Restaurant newRestaurant = restaurantService.createRestaurant(creator.getUsername(), restaurantCreationDto, pdfUrl);

        userService.addRestaurantAdmin(restaurantAdmin, newRestaurant);

        TableCreationDto tableCreationDto = TableCreationDto.builder()
                .restaurantId(newRestaurant.getId())
                .minPeople(0)
                .maxPeople(0)
                .build();

        tableService.createTable(newRestaurant, tableCreationDto);

        return restaurantMapper.restaurantToRestaurantResponseDto(newRestaurant);
    }

    @hasRestaurantAdminRole
    public void uploadImage(Long restaurantId, MultipartFile file) {
        checkRestaurantPermission(restaurantId);
        try {
            Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
            String imageUrl = cloudinaryService.uploadImage(file, "ResaurantImage" + restaurantId);
            restaurant.setImageUrl(imageUrl);
            restaurantService.save(restaurant);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @hasAdminRole
    public void uploadOwnershipProof(Long restaurantId, MultipartFile file) {
        try {
            Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
            String pdfUrl = cloudinaryService.uploadPdf(file, "ResaurantDocument" + restaurantId);
            restaurant.setOwnershipProofUrl(pdfUrl);
            restaurantService.save(restaurant);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Transactional
    @hasAdminRole
    public void acceptRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
        if (!RestaurantStatus.REQUESTED.equals(restaurant.getStatus())) {
            throw new BadRequestException("Cannot accept a restaurant that is not in REQUESTED status");
        }

        RestaurantModificationDto restaurantModificationDto = RestaurantModificationDto.builder()
                .status(RestaurantStatus.PRIVATE).build();
        restaurantService.updateRestaurant(restaurant, restaurantModificationDto);
    }

    @Transactional
    @hasAdminRole
    public void rejectRestaurant(Long restaurantId, String reason) {
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
        if (!RestaurantStatus.REQUESTED.equals(restaurant.getStatus())) {
            throw new BadRequestException("Cannot reject a restaurant that is not in REQUESTED status");
        }

        RestaurantModificationDto restaurantModificationDto = RestaurantModificationDto.builder()
                .status(RestaurantStatus.REJECTED)
                .rejectionMessage(reason).build();
        restaurantService.updateRestaurant(restaurant, restaurantModificationDto);
    }

    @hasAnyRole
    public RestaurantResponseDto getRestaurant(Long restaurantId) {
        return restaurantMapper.restaurantToRestaurantResponseDto(restaurantService.getRestaurant(restaurantId));
    }

    @hasAnyRole
    public List<RestaurantResponseDto> getRestaurants(RestaurantSearchDto restaurantSearchDto) {
        List<Restaurant> restaurants = restaurantService.getRestaurants(restaurantSearchDto);

        return restaurantMapper.restaurantListToRestaurantResponseDtoList(restaurants);
    }

    @hasRestaurantAdminRole
    public RestaurantResponseDto updateRestaurant(Long restaurantId, RestaurantModificationDto restaurantModificationDto) {
        checkRestaurantPermission(restaurantId);
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);

        restaurantService.updateRestaurant(restaurant, restaurantModificationDto);
        return restaurantMapper.restaurantToRestaurantResponseDto(restaurant);
    }

    @Transactional
    @hasRestaurantAdminRole
    public void deleteRestaurant(Long restaurantId) {
        checkRestaurantPermission(restaurantId);
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
        restaurant.getWorkers().forEach(userService::removeFromRestaurant);

        restaurantService.deleteRestaurant(restaurant);
    }

    public void checkRestaurantPermission(Long restaurantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        SavorlyUser user = userService.findUserByUsername(username);

        if (!SavorlyRole.ADMIN.equals(user.getRole()) && (user.getRestaurant() == null || !restaurantId.equals(user.getRestaurant().getId()))) {
            throw new ForbiddenException("You are not allowed to access this restaurant");
        }
    }
}
