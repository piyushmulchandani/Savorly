package com.application.savorly.dto.modify;

import com.application.savorly.domain.catalog.SavorlyRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModificationDto {

    @NotNull
    private String username;
    private SavorlyRole role;
    private String restaurantName;

}
