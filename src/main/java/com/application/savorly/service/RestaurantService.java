package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.entity.QRestaurant;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.RestaurantCreationDto;
import com.application.savorly.dto.modify.RestaurantModificationDto;
import com.application.savorly.dto.search.RestaurantSearchDto;
import com.application.savorly.repository.RestaurantRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

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

    public Restaurant findByName(String restaurantName) {
        return restaurantRepository.findByName(restaurantName)
                .orElseThrow(() -> new NotFoundException("Restaurant not found with name " + restaurantName));
    }

    public Restaurant createRestaurant(String username, RestaurantCreationDto restaurantCreationDto, String pdfUrl) {
        Restaurant restaurant = Restaurant.builder()
                .name(restaurantCreationDto.getName())
                .openTime(restaurantCreationDto.getOpenTime())
                .closeTime(restaurantCreationDto.getCloseTime())
                .cuisineType(restaurantCreationDto.getCuisineType())
                .description(restaurantCreationDto.getDescription())
                .address(restaurantCreationDto.getAddress())
                .phone(restaurantCreationDto.getPhone())
                .city(restaurantCreationDto.getCity())
                .country(restaurantCreationDto.getCountry())
                .creator(username)
                .ownershipProofUrl(pdfUrl)
                .build();
        return restaurantRepository.save(restaurant);
    }

    public void updateRestaurant(Restaurant restaurant, RestaurantModificationDto restaurantModificationDto) {
        restaurant.setStatus(restaurantModificationDto.getStatus() != null ? restaurantModificationDto.getStatus() : restaurant.getStatus());
        restaurant.setOpenTime(restaurantModificationDto.getOpenTime() != null ? restaurantModificationDto.getOpenTime() : restaurant.getOpenTime());
        restaurant.setCloseTime(restaurantModificationDto.getCloseTime() != null ? restaurantModificationDto.getCloseTime() : restaurant.getCloseTime());
        restaurant.setCuisineType(restaurantModificationDto.getCuisineType() != null ? restaurantModificationDto.getCuisineType() : restaurant.getCuisineType());
        restaurant.setDescription(restaurantModificationDto.getDescription() != null ? restaurantModificationDto.getDescription() : restaurant.getDescription());
        restaurant.setAddress(restaurantModificationDto.getAddress() != null ? restaurantModificationDto.getAddress() : restaurant.getAddress());
        restaurant.setPhone(restaurantModificationDto.getPhone() != null ? restaurantModificationDto.getPhone() : restaurant.getPhone());
        restaurant.setCity(restaurantModificationDto.getCity() != null ? restaurantModificationDto.getCity() : restaurant.getCity());
        restaurant.setCountry(restaurantModificationDto.getCountry() != null ? restaurantModificationDto.getCountry() : restaurant.getCountry());
        restaurant.setRejectionMessage(restaurantModificationDto.getRejectionMessage() != null ? restaurantModificationDto.getRejectionMessage() : restaurant.getRejectionMessage());

        restaurantRepository.save(restaurant);
    }

    public List<Restaurant> getRestaurants(RestaurantSearchDto restaurantSearchDto) {
        Predicate where = getWhere(restaurantSearchDto);
        List<Restaurant> restaurants = (List<Restaurant>) restaurantRepository.findAll(where);

        if (restaurantSearchDto.getDateTime() != null && restaurantSearchDto.getNumPeople() != null) {
            restaurants = filterByReservation(restaurants, restaurantSearchDto.getDateTime(), restaurantSearchDto.getNumPeople());
        }

        return restaurants;
    }

    public void deleteRestaurant(Restaurant restaurant) {
        restaurantRepository.delete(restaurant);
    }

    private Predicate getWhere(RestaurantSearchDto restaurantSearchDto) {
        BooleanBuilder where = new BooleanBuilder();

        if (restaurantSearchDto.getName() != null) {
            where.and(QRestaurant.restaurant.name.contains(restaurantSearchDto.getName()));
        }
        if (restaurantSearchDto.getStatus() != null) {
            where.and(QRestaurant.restaurant.status.eq(restaurantSearchDto.getStatus()));
        }
        if (restaurantSearchDto.getCuisineType() != null) {
            where.and(QRestaurant.restaurant.cuisineType.eq(restaurantSearchDto.getCuisineType()));
        }
        if (restaurantSearchDto.getCity() != null) {
            where.and(QRestaurant.restaurant.city.equalsIgnoreCase(restaurantSearchDto.getCity()));
        }

        return where;
    }

    private List<Restaurant> filterByReservation(List<Restaurant> restaurants, LocalDateTime dateTime, Integer numPeople) {
        LocalDateTime endDateTime = dateTime.plusMinutes(90);

        return restaurants.stream()
                .filter(restaurant -> restaurant.getTables().stream().anyMatch(table ->
                        numPeople >= table.getMinPeople() && numPeople <= table.getMaxPeople()
                                && isTableAvailable(table, dateTime, endDateTime)
                ))
                .toList();
    }

    private boolean isTableAvailable(Table table, LocalDateTime start, LocalDateTime end) {
        return table.getReservations().stream().noneMatch(reservation -> {
            LocalDateTime resStart = reservation.getReservationTime();
            LocalDateTime resEnd = resStart.plusMinutes(90);
            return resStart.isBefore(end) && resEnd.isAfter(start);
        });
    }
}
