package com.application.savorly.controller;

import com.application.savorly.SavorlyApplication;
import com.application.savorly.config.interfaces.WithMockCustomUser;
import com.application.savorly.domain.catalog.OrderType;
import com.application.savorly.domain.catalog.SavorlyRole;
import com.application.savorly.domain.entity.*;
import com.application.savorly.dto.create.OrderCreationDto;
import com.application.savorly.dto.response.OrderResponseDto;
import com.application.savorly.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(classes = {SavorlyApplication.class})
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:/application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    @WithMockCustomUser(role = "restaurant_worker")
    void createOrder() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("Username")
                .role(SavorlyRole.RESTAURANT_WORKER)
                .build();
        restaurant.addWorker(user);
        userRepository.save(user);

        Table table = Table.builder()
                .tableNumber(1)
                .build();
        restaurant.addTable(table);
        table = tableRepository.save(table);

        Product product = Product.builder()
                .name("product")
                .price(BigDecimal.TEN)
                .build();
        restaurant.addProduct(product);
        product = productRepository.save(product);

        OrderCreationDto orderCreationDto = OrderCreationDto.builder()
                .restaurantId(restaurant.getId())
                .tableNumber(table.getTableNumber())
                .type(OrderType.RESTAURANT)
                .productIds(List.of(product.getId()))
                .build();

        mockMvc.perform(post("/api/v1/restaurants/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderCreationDto)))
                .andExpect(status().isOk());

        List<Order> orders = orderRepository.findAll();
        table = tableRepository.findById(table.getId()).orElseThrow();

        assertThat(orders).hasSize(1);

        Order order = orders.getFirst();
        assertThat(order.getTable()).isEqualTo(table);
        assertThat(order.getOrderTime()).isNotNull();
        assertThat(order.getType()).isEqualTo(OrderType.RESTAURANT);
        assertThat(order.getCompleted()).isFalse();
        assertThat(order.getProducts()).hasSize(1);
        assertThat(table.getCurrentCost()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_worker")
    void confirmOrder() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("Username")
                .role(SavorlyRole.RESTAURANT_WORKER)
                .build();
        restaurant.addWorker(user);
        userRepository.save(user);

        Table table = Table.builder()
                .tableNumber(1)
                .build();
        restaurant.addTable(table);
        table = tableRepository.save(table);

        Product product = Product.builder()
                .name("product")
                .price(BigDecimal.TEN)
                .build();
        restaurant.addProduct(product);
        product = productRepository.save(product);

        List<Product> products = new ArrayList<>();
        products.add(product);

        Order order = Order.builder()
                .products(products)
                .build();
        table.addOrder(order);
        order = orderRepository.save(order);

        mockMvc.perform(patch("/api/v1/restaurants/orders/confirm/{orderId}", order.getId()))
                .andExpect(status().isOk());

        order = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(order.getCompleted()).isTrue();
    }

    @Test
    @WithMockCustomUser(role = "restaurant_worker")
    void getAllOrders_Unfiltered() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        Table table = Table.builder()
                .tableNumber(1)
                .build();
        restaurant.addTable(table);
        table = tableRepository.save(table);

        Product product = Product.builder()
                .name("product")
                .price(BigDecimal.TEN)
                .build();
        restaurant.addProduct(product);
        product = productRepository.save(product);

        List<Product> products = new ArrayList<>();
        products.add(product);

        Order order = Order.builder()
                .type(OrderType.RESTAURANT)
                .products(products)
                .build();
        table.addOrder(order);
        orderRepository.save(order);

        Table table2 = Table.builder()
                .tableNumber(1)
                .build();
        restaurant.addTable(table2);
        tableRepository.save(table2);

        Order order2 = Order.builder()
                .type(OrderType.DELIVERY)
                .products(products)
                .build();
        table2.addOrder(order2);
        orderRepository.save(order2);

        MvcResult actual = mockMvc.perform(get("/api/v1/restaurants/orders")
                        .queryParam("restaurantId", restaurant.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        List<OrderResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(2);
        assertThat(response.getFirst().getOrderTime()).isNotNull();
        assertThat(response.getFirst().getCompleted()).isNotNull();
        assertThat(response.getFirst().getTableNumber()).isNotNull();
        assertThat(response.getFirst().getType()).isNotNull();
        assertThat(response.getFirst().getProducts()).isNotEmpty();
    }

    @Test
    @WithMockCustomUser(role = "restaurant_worker")
    void getAllOrders_Filtered() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        Table table = Table.builder()
                .tableNumber(1)
                .build();
        restaurant.addTable(table);
        table = tableRepository.save(table);

        Product product = Product.builder()
                .name("product")
                .price(BigDecimal.TEN)
                .build();
        restaurant.addProduct(product);
        product = productRepository.save(product);

        List<Product> products = new ArrayList<>();
        products.add(product);

        Order order = Order.builder()
                .type(OrderType.RESTAURANT)
                .products(products)
                .build();
        table.addOrder(order);
        orderRepository.save(order);

        Table table2 = Table.builder()
                .tableNumber(1)
                .build();
        restaurant.addTable(table2);
        tableRepository.save(table2);

        Order order2 = Order.builder()
                .type(OrderType.DELIVERY)
                .products(products)
                .build();
        table2.addOrder(order2);
        orderRepository.save(order2);

        MvcResult actual = mockMvc.perform(get("/api/v1/restaurants/orders")
                        .queryParam("restaurantId", restaurant.getId().toString())
                        .queryParam("orderType", OrderType.RESTAURANT.toString())
                        .queryParam("tableNumber", String.valueOf(table.getTableNumber()))
                        .queryParam("completed", Boolean.FALSE.toString()))
                .andExpect(status().isOk())
                .andReturn();

        List<OrderResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(1);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_worker")
    void cancelOrder() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("Username")
                .role(SavorlyRole.RESTAURANT_WORKER)
                .build();
        restaurant.addWorker(user);
        userRepository.save(user);

        Table table = Table.builder()
                .tableNumber(1)
                .currentCost(BigDecimal.TEN)
                .build();
        restaurant.addTable(table);
        table = tableRepository.save(table);

        Product product = Product.builder()
                .name("product")
                .price(BigDecimal.TEN)
                .build();
        restaurant.addProduct(product);
        product = productRepository.save(product);

        List<Product> products = new ArrayList<>();
        products.add(product);

        Order order = Order.builder()
                .products(products)
                .build();
        table.addOrder(order);
        order = orderRepository.save(order);

        mockMvc.perform(delete("/api/v1/restaurants/orders/cancel/{orderId}", order.getId()))
                .andExpect(status().isOk());

        table = tableRepository.findById(table.getId()).orElseThrow();

        assertThat(orderRepository.findById(order.getId())).isEmpty();
        assertThat(table.getCurrentCost()).isEqualTo(BigDecimal.ZERO);
    }
}