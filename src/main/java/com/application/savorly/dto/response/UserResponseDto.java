package com.application.savorly.dto.response;

import com.application.savorly.domain.catalog.SavorlyRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {

    private String username;
    private SavorlyRole role;
    private RestaurantResponseDto restaurant;
    private Date lastLogonDate;
    private List<ReservationResponseDto> reservations;

}
