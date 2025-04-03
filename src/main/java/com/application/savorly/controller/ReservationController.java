package com.application.savorly.controller;

import com.application.savorly.facade.ReservationFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
//@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) needed for request
    private ReservationFacade reservationFacade;

    public ReservationController(ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }
}
