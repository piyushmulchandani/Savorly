package com.application.savorly.dto;

import com.application.savorly.domain.catalog.SavorlyRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSearchParamsDto {
    private String username;
    private SavorlyRole role;
}
