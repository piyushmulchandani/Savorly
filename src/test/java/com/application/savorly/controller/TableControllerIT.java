package com.application.savorly.controller;

import com.application.savorly.SavorlyApplication;
import com.application.savorly.config.interfaces.WithMockCustomUser;
import com.application.savorly.domain.catalog.SavorlyRole;
import com.application.savorly.domain.entity.Order;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.TableCreationDto;
import com.application.savorly.dto.response.TableResponseDto;
import com.application.savorly.repository.OrderRepository;
import com.application.savorly.repository.RestaurantRepository;
import com.application.savorly.repository.TableRepository;
import com.application.savorly.repository.UserRepository;
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
class TableControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @WithMockCustomUser(role = "restaurant_admin")
    void addTable() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("Username")
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .build();
        restaurant.addWorker(user);
        userRepository.save(user);

        TableCreationDto tableCreationDto = TableCreationDto.builder()
                .restaurantId(restaurant.getId())
                .maxPeople(4)
                .minPeople(3)
                .build();

        mockMvc.perform(post("/api/v1/restaurants/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tableCreationDto)))
                .andExpect(status().isOk());

        List<Table> tables = tableRepository.findAll();
        Table table = tables.getFirst();

        assertThat(table.getTableNumber()).isZero();
        assertThat(table.getRestaurant()).isEqualTo(restaurant);
        assertThat(table.getMaxPeople()).isEqualTo(4);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin")
    void getAllTables_Unfiltered() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        Table table1 = Table.builder()
                .tableNumber(0)
                .maxPeople(4)
                .minPeople(3)
                .build();
        restaurant.addTable(table1);
        tableRepository.save(table1);

        Table table2 = Table.builder()
                .tableNumber(1)
                .maxPeople(4)
                .minPeople(3)
                .build();
        restaurant.addTable(table2);
        tableRepository.save(table2);

        MvcResult actual = mockMvc.perform(get("/api/v1/restaurants/tables")
                        .queryParam("restaurantId", restaurant.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        List<TableResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(2);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_worker")
    void getAllTables_Filtered() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        Table table1 = Table.builder()
                .tableNumber(0)
                .maxPeople(4)
                .minPeople(3)
                .build();
        restaurant.addTable(table1);
        tableRepository.save(table1);

        Table table2 = Table.builder()
                .tableNumber(1)
                .maxPeople(4)
                .minPeople(3)
                .build();
        restaurant.addTable(table2);
        tableRepository.save(table2);

        MvcResult actual = mockMvc.perform(get("/api/v1/restaurants/tables")
                        .queryParam("restaurantId", restaurant.getId().toString())
                        .queryParam("numPeople", Integer.toString(3))
                        .queryParam("occupied", Boolean.FALSE.toString())
                        .queryParam("tableNumber", Integer.toString(1)))
                .andExpect(status().isOk())
                .andReturn();

        List<TableResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(1);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_worker")
    void occupyTable() throws Exception {
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
                .tableNumber(0)
                .maxPeople(4)
                .minPeople(3)
                .build();
        restaurant.addTable(table);
        table = tableRepository.save(table);

        mockMvc.perform(patch("/api/v1/restaurants/tables/occupy/{tableId}", table.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(role = "restaurant_worker")
    void completeService() throws Exception {
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
                .occupied(true)
                .maxPeople(4)
                .minPeople(3)
                .build();
        restaurant.addTable(table);
        table = tableRepository.save(table);

        Order order = Order.builder()
                .build();

        table.addOrder(order);
        orderRepository.save(order);

        mockMvc.perform(patch("/api/v1/restaurants/tables/complete/{tableId}", table.getId()))
                .andExpect(status().isOk());

        table = tableRepository.findById(table.getId()).orElseThrow();

        assertThat(orderRepository.findById(order.getId())).isEmpty();
        assertThat(table.getCurrentCost()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin")
    void removeTable() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("Username")
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .build();
        restaurant.addWorker(user);
        userRepository.save(user);

        Table table1 = Table.builder()
                .tableNumber(0)
                .maxPeople(4)
                .minPeople(3)
                .build();
        restaurant.addTable(table1);
        tableRepository.save(table1);

        Table table2 = Table.builder()
                .tableNumber(1)
                .maxPeople(4)
                .minPeople(3)
                .build();
        restaurant.addTable(table2);
        tableRepository.save(table2);

        mockMvc.perform(delete("/api/v1/restaurants/tables/{restaurantId}", restaurant.getId().toString()))
                .andExpect(status().isOk());

        restaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();

        assertThat(restaurant.getTables()).hasSize(1);
    }

}