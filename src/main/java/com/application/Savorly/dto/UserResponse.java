package com.application.Savorly.dto;

import com.application.Savorly.domain.catalog.SavorlyRole;
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
