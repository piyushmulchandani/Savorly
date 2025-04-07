package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.catalog.OrderType;
import com.application.savorly.domain.entity.Order;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
    }

    public Order createOrder(Table table, List<Product> products, OrderType type) {
        Order order = Order.builder()
                .type(type)
                .table(table)
                .products(products)
                .build();

        return orderRepository.save(order);
    }

    public void confirmOrder(Order order) {
        order.setCompleted(true);

        orderRepository.save(order);
    }
}
