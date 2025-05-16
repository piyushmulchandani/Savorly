package com.application.savorly.facade;

import com.application.savorly.config.interfaces.hasRestaurantRole;
import com.application.savorly.domain.entity.Order;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.OrderCreationDto;
import com.application.savorly.dto.response.OrderResponseDto;
import com.application.savorly.dto.search.OrderSearchDto;
import com.application.savorly.mapper.OrderMapper;
import com.application.savorly.service.OrderService;
import com.application.savorly.service.ProductService;
import com.application.savorly.service.TableService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderFacade {

    private final OrderService orderService;
    private final TableService tableService;
    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final RestaurantFacade restaurantFacade;

    public OrderFacade(OrderMapper orderMapper, OrderService orderService, TableService tableService, ProductService productService, RestaurantFacade restaurantFacade) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.tableService = tableService;
        this.productService = productService;
        this.restaurantFacade = restaurantFacade;
    }

    @hasRestaurantRole
    public OrderResponseDto createOrder(OrderCreationDto orderCreationDto) {
        Table table = tableService.findByTableNumber(orderCreationDto.getRestaurantId(), orderCreationDto.getTableNumber());
        restaurantFacade.checkRestaurantPermission(table.getRestaurant().getId());

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
        restaurantFacade.checkRestaurantPermission(order.getTable().getRestaurant().getId());

        orderService.confirmOrder(order);
    }

    @hasRestaurantRole
    public List<OrderResponseDto> getAllOrders(OrderSearchDto orderSearchDto) {
        return orderMapper.ordersToOrderResponseDtos(orderService.getAllOrdersFiltered(orderSearchDto));
    }

    @Transactional
    @hasRestaurantRole
    public void cancelOrder(Long orderId) {
        Order order = orderService.findById(orderId);
        restaurantFacade.checkRestaurantPermission(order.getTable().getRestaurant().getId());

        BigDecimal orderCost = order.getProducts().stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        tableService.addCost(order.getTable(), orderCost.negate());

        orderService.cancelOrder(order);
    }
}
