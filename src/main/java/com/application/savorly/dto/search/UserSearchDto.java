package com.application.savorly.dto.search;

import com.application.savorly.domain.catalog.SavorlyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDto {
    private String username;
    private SavorlyRole role;
    private String restaurantName;
}
