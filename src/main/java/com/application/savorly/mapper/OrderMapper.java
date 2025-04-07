package com.application.savorly.mapper;

import com.application.savorly.domain.entity.Order;
import com.application.savorly.dto.response.OrderResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface OrderMapper {
    @Mapping(target = "tableNumber", source = "table.tableNumber")
    OrderResponseDto orderToOrderResponseDto(Order order);

    List<OrderResponseDto> ordersToOrderResponseDtos(List<Order> orders);
}
