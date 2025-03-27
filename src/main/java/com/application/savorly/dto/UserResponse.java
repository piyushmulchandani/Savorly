package com.application.savorly.dto;

import com.application.savorly.domain.catalog.SavorlyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String username;
    private SavorlyRole role;
    private Date lastLogonDate;

}
