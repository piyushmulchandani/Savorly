package com.application.savorly.facade;

import com.application.savorly.mapper.OrderMapper;
import com.application.savorly.service.OrderService;
import org.springframework.stereotype.Component;

@Component
public class OrderFacade {

    private OrderService orderService;
    private OrderMapper orderMapper;

    public OrderFacade(OrderMapper orderMapper, OrderService orderService) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
    }
}
