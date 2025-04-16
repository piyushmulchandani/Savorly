package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.entity.*;
import com.application.savorly.dto.create.ReservationCreationDto;
import com.application.savorly.dto.search.ReservationSearchDto;
import com.application.savorly.repository.ReservationRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation findById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation with following ID not found: " + reservationId));
    }

    public List<LocalTime> getAvailableReservationTimes(Restaurant restaurant, ReservationSearchDto reservationSearchDto) {

        LocalTime openingTime = restaurant.getOpenTime();
        LocalTime closingTime = restaurant.getCloseTime();

        List<LocalTime> availableTimes = new ArrayList<>();

        for (LocalTime time = openingTime;
             time.plusMinutes(90).isBefore(closingTime) || time.plusMinutes(90).equals(closingTime);
             time = time.plusMinutes(15)) {

            LocalTime finalTime = time;
            boolean anyTableAvailable = restaurant.getTables().stream()
                    .filter(table -> table.getMinPeople() <= reservationSearchDto.getNumPeople()
                            && table.getMaxPeople() >= reservationSearchDto.getNumPeople())
                    .anyMatch(table -> table.getReservations().stream()
                            .noneMatch(reservation -> {
                                LocalTime resStart = LocalTime.from(reservation.getReservationTime());
                                LocalTime resEnd = resStart.plusMinutes(90);
                                return !(finalTime.plusMinutes(90).isBefore(resStart) || finalTime.isAfter(resEnd));
                            }));

            if (anyTableAvailable) {
                availableTimes.add(time);
            }
        }

        return availableTimes;
    }

    public Reservation createReservation(Restaurant restaurant, SavorlyUser user, Table table, ReservationCreationDto reservationCreationDto) {
        Reservation reservation = Reservation.builder()
                .reservationTime(reservationCreationDto.getDateTime())
                .numPeople(reservationCreationDto.getNumPeople())
                .build();
        restaurant.addReservation(reservation);
        user.addReservation(reservation);
        table.addReservation(reservation);

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservations(ReservationSearchDto reservationSearchDto) {
        Predicate predicate = getWhere(reservationSearchDto);
        return (List<Reservation>) reservationRepository.findAll(predicate);
    }

    private Predicate getWhere(ReservationSearchDto reservationSearchDto) {
        BooleanBuilder where = new BooleanBuilder();

        if(reservationSearchDto.getRestaurantId() != null) {
            where.and(QReservation.reservation.restaurant.id.eq(reservationSearchDto.getRestaurantId()));
        }
        if(reservationSearchDto.getUsername() != null) {
            where.and(QReservation.reservation.user.username.eq(reservationSearchDto.getUsername()));
        }
        if(reservationSearchDto.getDate() != null) {
            LocalDate date = reservationSearchDto.getDate();
            where.and(QReservation.reservation.reservationTime.year().eq(date.getYear()));
            where.and(QReservation.reservation.reservationTime.month().eq(date.getMonthValue()));
            where.and(QReservation.reservation.reservationTime.dayOfMonth().eq(date.getDayOfMonth()));
        }

        return where;
    }

    public void cancelReservation(Reservation reservation) {
        reservation.getRestaurant().getReservations().remove(reservation);
        reservation.getTable().getReservations().remove(reservation);
        reservation.getUser().getReservations().remove(reservation);
        reservationRepository.delete(reservation);
    }
}
