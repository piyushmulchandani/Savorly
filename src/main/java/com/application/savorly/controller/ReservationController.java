package com.application.savorly.controller;

import com.application.savorly.dto.create.ReservationCreationDto;
import com.application.savorly.dto.response.ReservationResponseDto;
import com.application.savorly.dto.search.ReservationSearchDto;
import com.application.savorly.facade.ReservationFacade;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants/reservations")
public class ReservationController {

    private final ReservationFacade reservationFacade;

    public ReservationController(ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    @GetMapping("/available-times")
    public List<LocalTime> getAvailableTimesForRestaurant(
            @ParameterObject ReservationSearchDto reservationSearchDto
            ) {
        return reservationFacade.getAvailableTimeSlots(reservationSearchDto);
    }

    @PostMapping
    public ReservationResponseDto createReservation(
            @RequestBody @Valid ReservationCreationDto reservationCreationDto
            ) {
        return reservationFacade.createReservation(reservationCreationDto);
    }

    @GetMapping
    public List<ReservationResponseDto> getReservations(
            @ParameterObject ReservationSearchDto reservationSearchDto
    ) {
        if(reservationSearchDto.getRestaurantId() != null){
            return reservationFacade.getRestaurantReservations(reservationSearchDto);
        }

        return reservationFacade.getPersonalReservations(reservationSearchDto);
    }

    @DeleteMapping("/{reservationId}")
    public void cancelReservation(
            @PathVariable Long reservationId
    ) {
        reservationFacade.cancelReservation(reservationId);
    }
}
