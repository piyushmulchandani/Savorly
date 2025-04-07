package com.application.savorly.facade;

import com.application.savorly.config.interfaces.hasRestaurantRole;
import com.application.savorly.domain.entity.Order;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.OrderCreationDto;
import com.application.savorly.dto.response.OrderResponseDto;
import com.application.savorly.mapper.OrderMapper;
import com.application.savorly.service.OrderService;
import com.application.savorly.service.ProductService;
import com.application.savorly.service.TableService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderFacade {

    private final OrderService orderService;
    private final TableService tableService;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    public OrderFacade(OrderMapper orderMapper, OrderService orderService, TableService tableService, ProductService productService) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.tableService = tableService;
        this.productService = productService;
    }

    @hasRestaurantRole
    public OrderResponseDto createOrder(Long tableId, OrderCreationDto orderCreationDto) {
        Table table = tableService.findById(tableId);

        List<Product> products = orderCreationDto.getProductIds().stream()
                .map(productService::findById)
                .toList();

        BigDecimal orderCost = products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        tableService.addCost(table, orderCost);

        return orderMapper.orderToOrderResponseDto(orderService.createOrder(table, products, orderCreationDto.getType()));
    }

    @hasRestaurantRole
    public void confirmOrder(Long orderId) {
        Order order = orderService.findById(orderId);
        orderService.confirmOrder(order);
    }
}
