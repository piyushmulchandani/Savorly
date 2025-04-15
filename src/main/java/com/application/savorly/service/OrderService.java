package com.application.savorly.service;

import com.application.savorly.config.exceptions.NotFoundException;
import com.application.savorly.domain.catalog.OrderType;
import com.application.savorly.domain.entity.Order;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.QOrder;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.search.OrderSearchDto;
import com.application.savorly.repository.OrderRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
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
                .products(products)
                .build();

        table.addOrder(order);

        return orderRepository.save(order);
    }

    public void confirmOrder(Order order) {
        order.setCompleted(true);

        orderRepository.save(order);
    }

    public List<Order> getAllOrdersFiltered(OrderSearchDto orderSearchDto) {
        Predicate predicate = getWhere(orderSearchDto);

        return (List<Order>) orderRepository.findAll(predicate);
    }

    public void cancelOrder(Order order) {
        order.getTable().getOrders().remove(order);
        orderRepository.deleteById(order.getId());
    }

    private Predicate getWhere(OrderSearchDto orderSearchDto) {
        BooleanBuilder where = new BooleanBuilder();

        where.and(QOrder.order.table.restaurant.id.eq(orderSearchDto.getRestaurantId()));

        if(orderSearchDto.getOrderType() != null) {
            where.and(QOrder.order.type.eq(orderSearchDto.getOrderType()));
        }

        if(orderSearchDto.getTableId() != null) {
            where.and(QOrder.order.table.id.eq(orderSearchDto.getTableId()));
        }

        if(orderSearchDto.getCompleted() != null) {
            where.and(QOrder.order.completed.eq(orderSearchDto.getCompleted()));
        }

        return where;
    }
}
