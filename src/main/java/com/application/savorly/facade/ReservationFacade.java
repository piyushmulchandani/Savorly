package com.application.savorly.facade;

import com.application.savorly.mapper.ReservationMapper;
import com.application.savorly.service.ReservationService;
import org.springframework.stereotype.Component;

@Component
public class ReservationFacade {

    private ReservationService reservationService;
    private ReservationMapper reservationMapper;

    public ReservationFacade(ReservationService reservationService, ReservationMapper reservationMapper) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
    }
}
