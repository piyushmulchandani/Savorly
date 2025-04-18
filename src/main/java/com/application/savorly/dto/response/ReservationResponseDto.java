package com.application.savorly.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationResponseDto {

    private Long id;
    private LocalDateTime reservationTime;
    private Integer numPeople;
    private String username;
    private String restaurantName;
    private Integer tableNumber;

}
