package com.application.savorly.controller;

import com.application.savorly.dto.create.OrderCreationDto;
import com.application.savorly.dto.response.OrderResponseDto;
import com.application.savorly.facade.OrderFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderFacade orderFacade;

    public OrderController(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @PostMapping("/{tableId}")
    public OrderResponseDto createOrder(
            @PathVariable Long tableId,
            @RequestBody OrderCreationDto orderCreationDto
    ) {
        return orderFacade.createOrder(tableId, orderCreationDto);
    }

    @PostMapping("/confirm/{orderId}")
    public void confirmOrder(
            @PathVariable Long orderId
    ) {
        orderFacade.confirmOrder(orderId);
    }
}
