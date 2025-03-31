package com.application.savorly.mapper;

import com.application.savorly.domain.entity.Reservation;
import com.application.savorly.dto.response.ReservationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "restaurant.name", target = "restaurantName")
    ReservationResponseDto reservationToReservationResponseDto(Reservation reservation);

    List<ReservationResponseDto> reservationsToReservationResponseDtos(List<Reservation> reservations);
}
