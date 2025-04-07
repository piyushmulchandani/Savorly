package com.application.savorly.controller;

import com.application.savorly.SavorlyApplication;
import com.application.savorly.config.interfaces.WithMockCustomUser;
import com.application.savorly.domain.catalog.OrderType;
import com.application.savorly.domain.entity.Order;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.OrderCreationDto;
import com.application.savorly.repository.OrderRepository;
import com.application.savorly.repository.ProductRepository;
import com.application.savorly.repository.RestaurantRepository;
import com.application.savorly.repository.TableRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(classes = {SavorlyApplication.class})
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:/application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

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

        Table table = Table.builder()
                .tableNumber(1)
                .restaurant(restaurant)
                .build();
        table = tableRepository.save(table);

        Product product = Product.builder()
                .name("product")
                .price(BigDecimal.TEN)
                .restaurant(restaurant)
                .build();
        product = productRepository.save(product);

        OrderCreationDto orderCreationDto = OrderCreationDto.builder()
                .type(OrderType.RESTAURANT)
                .productIds(List.of(product.getId()))
                .build();

        mockMvc.perform(post("/api/v1/orders/{tableId}", table.getId())
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
}