package com.application.savorly.dto.create;

import com.application.savorly.domain.catalog.OrderType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreationDto {
    @NotNull
    private Long tableId;
    @NotNull
    private OrderType type;
    @NotEmpty
    private List<Long> productIds;
}
