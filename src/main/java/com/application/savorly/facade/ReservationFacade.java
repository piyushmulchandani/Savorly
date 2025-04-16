package com.application.savorly.facade;

import com.application.savorly.config.interfaces.hasAnyRole;
import com.application.savorly.config.interfaces.hasRestaurantRole;
import com.application.savorly.domain.entity.Reservation;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.ReservationCreationDto;
import com.application.savorly.dto.response.ReservationResponseDto;
import com.application.savorly.dto.search.ReservationSearchDto;
import com.application.savorly.mapper.ReservationMapper;
import com.application.savorly.service.ReservationService;
import com.application.savorly.service.RestaurantService;
import com.application.savorly.service.TableService;
import com.application.savorly.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Component
public class ReservationFacade {

    private final ReservationService reservationService;
    private final RestaurantService restaurantService;
    private final TableService tableService;
    private final ReservationMapper reservationMapper;
    private final UserService userService;

    public ReservationFacade(ReservationService reservationService, RestaurantService restaurantService, TableService tableService, ReservationMapper reservationMapper, UserService userService) {
        this.reservationService = reservationService;
        this.restaurantService = restaurantService;
        this.tableService = tableService;
        this.reservationMapper = reservationMapper;
        this.userService = userService;
    }

    @hasAnyRole
    public List<LocalTime> getAvailableTimeSlots(ReservationSearchDto reservationSearchDto) {
        Restaurant restaurant = restaurantService.getRestaurant(reservationSearchDto.getRestaurantId());
        return reservationService.getAvailableReservationTimes(restaurant, reservationSearchDto);
    }

    @hasAnyRole
    public ReservationResponseDto createReservation(ReservationCreationDto reservationCreationDto) {
        Restaurant restaurant = restaurantService.getRestaurant(reservationCreationDto.getRestaurantId());

        Table table = tableService.findAvailableTableFor(
                restaurant.getId(),
                reservationCreationDto.getDateTime(),
                reservationCreationDto.getNumPeople()
        );

        SavorlyUser user = userService.findUserByUsername(reservationCreationDto.getUsername());

        Reservation reservation = reservationService.createReservation(restaurant, user, table, reservationCreationDto);

        return reservationMapper.reservationToReservationResponseDto(reservation);
    }

    @hasRestaurantRole
    public List<ReservationResponseDto> getRestaurantReservations(ReservationSearchDto reservationSearchDto) {
        return reservationMapper.reservationsToReservationResponseDtos(reservationService.getAllReservations(reservationSearchDto));
    }

    @hasAnyRole
    public List<ReservationResponseDto> getPersonalReservations(ReservationSearchDto reservationSearchDto) {
        return reservationMapper.reservationsToReservationResponseDtos(reservationService.getAllReservations(reservationSearchDto));
    }

    @Transactional
    @hasAnyRole
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationService.findById(reservationId);
        reservationService.cancelReservation(reservation);
    }
}
