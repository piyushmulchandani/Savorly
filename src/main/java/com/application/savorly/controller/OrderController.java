package com.application.savorly.controller;

import com.application.savorly.dto.create.OrderCreationDto;
import com.application.savorly.dto.response.OrderResponseDto;
import com.application.savorly.dto.search.OrderSearchDto;
import com.application.savorly.facade.OrderFacade;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants/orders")
public class OrderController {

    private final OrderFacade orderFacade;

    public OrderController(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @PostMapping
    public OrderResponseDto createOrder(
            @RequestBody OrderCreationDto orderCreationDto
    ) {
        return orderFacade.createOrder(orderCreationDto);
    }

    @PatchMapping("/confirm/{orderId}")
    public void confirmOrder(
            @PathVariable Long orderId
    ) {
        orderFacade.confirmOrder(orderId);
    }

    @GetMapping
    public List<OrderResponseDto> getAllOrders(
            @ParameterObject OrderSearchDto orderSearchDto
    ) {
        return orderFacade.getAllOrders(orderSearchDto);
    }

    @DeleteMapping("/cancel/{orderId}")
    public void cancelOrder(
            @PathVariable Long orderId
    ) {
        orderFacade.cancelOrder(orderId);
    }
}
