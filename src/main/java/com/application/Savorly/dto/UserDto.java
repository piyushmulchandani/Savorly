package com.application.Savorly.dto;

import com.application.Savorly.domain.catalog.SavorlyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
    private SavorlyRole role;
}
