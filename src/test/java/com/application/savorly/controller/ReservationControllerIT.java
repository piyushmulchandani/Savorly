package com.application.savorly.controller;

import com.application.savorly.SavorlyApplication;
import com.application.savorly.config.interfaces.WithMockCustomUser;
import com.application.savorly.domain.entity.*;
import com.application.savorly.dto.create.ReservationCreationDto;
import com.application.savorly.dto.response.ReservationResponseDto;
import com.application.savorly.repository.ReservationRepository;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
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
class ReservationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    private final LocalTime openTime = LocalTime.of(8, 0);
    private final LocalTime closeTime = LocalTime.of(21, 0);

    @Test
    @WithMockCustomUser
    void getAvailableTimeSlots() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        Table table = Table.builder()
                .minPeople(2)
                .maxPeople(3)
                .build();
        restaurant.addTable(table);
        tableRepository.save(table);

        MvcResult actual = mockMvc.perform(get("/api/v1/restaurants/reservations/available-times")
                        .queryParam("restaurantId", restaurant.getId().toString())
                        .queryParam("numPeople", Integer.toString(2))
                        .queryParam("date", "2025-04-01"))
                .andExpect(status().isOk())
                .andReturn();

        List<LocalTime> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).isNotEmpty();
        assertThat(response.getFirst()).isEqualTo(openTime);
    }

    @Test
    @WithMockCustomUser
    void createReservation() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("User")
                .build();
        userRepository.save(user);

        Table table = Table.builder()
                .minPeople(2)
                .maxPeople(3)
                .build();
        restaurant.addTable(table);
        tableRepository.save(table);

        ReservationCreationDto reservationCreationDto = ReservationCreationDto.builder()
                .restaurantId(restaurant.getId())
                .username("User")
                .dateTime(LocalDateTime.of(2025, 4, 1, 7, 30))
                .numPeople(2)
                .build();

        mockMvc.perform(post("/api/v1/restaurants/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationCreationDto)))
                .andExpect(status().isOk());

        List<Reservation> reservations = reservationRepository.findAll();
        restaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();

        assertThat(reservations).hasSize(1);

        assertThat(reservations.getFirst().getReservationTime()).isNotNull();
        assertThat(reservations.getFirst().getNumPeople()).isEqualTo(2);
        assertThat(reservations.getFirst().getUser().getUsername()).isEqualTo("User");
        assertThat(reservations.getFirst().getRestaurant()).isEqualTo(restaurant);

        assertThat(restaurant.getReservations()).hasSize(1);
    }

    @Test
    @WithMockCustomUser
    void createReservation_NotAvailableTime() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("User")
                .build();
        userRepository.save(user);

        Table table = Table.builder()
                .minPeople(2)
                .maxPeople(3)
                .build();
        restaurant.addTable(table);
        tableRepository.save(table);

        Reservation reservation = Reservation.builder()
                .reservationTime(LocalDateTime.of(2025, 4, 1, 7, 45))
                .numPeople(2)
                .build();
        restaurant.addReservation(reservation);
        table.addReservation(reservation);
        user.addReservation(reservation);
        reservationRepository.save(reservation);

        ReservationCreationDto reservationCreationDto = ReservationCreationDto.builder()
                .restaurantId(restaurant.getId())
                .username("User")
                .dateTime(LocalDateTime.of(2025, 4, 1, 7, 30))
                .numPeople(2)
                .build();

        mockMvc.perform(post("/api/v1/restaurants/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationCreationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(role = "restaurant_worker")
    void getRestaurantReservations() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("User")
                .build();
        userRepository.save(user);

        Table table = Table.builder()
                .minPeople(2)
                .maxPeople(3)
                .build();
        restaurant.addTable(table);
        tableRepository.save(table);

        Reservation reservation = Reservation.builder()
                .reservationTime(LocalDateTime.of(2025, 4, 1, 7, 45))
                .numPeople(2)
                .build();
        restaurant.addReservation(reservation);
        table.addReservation(reservation);
        user.addReservation(reservation);
        reservationRepository.save(reservation);

        reservation = Reservation.builder()
                .reservationTime(LocalDateTime.of(2025, 4, 2, 7, 45))
                .numPeople(2)
                .build();
        restaurant.addReservation(reservation);
        table.addReservation(reservation);
        user.addReservation(reservation);
        reservationRepository.save(reservation);

        Restaurant restaurant2 = Restaurant.builder()
                .name("Restaurant2")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant2 = restaurantRepository.save(restaurant2);

        reservation = Reservation.builder()
                .reservationTime(LocalDateTime.of(2025, 4, 1, 7, 45))
                .numPeople(2)
                .build();
        restaurant2.addReservation(reservation);
        table.addReservation(reservation);
        user.addReservation(reservation);
        reservationRepository.save(reservation);

        MvcResult actual = mockMvc.perform(get("/api/v1/restaurants/reservations")
                        .queryParam("restaurantId", restaurant.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        List<ReservationResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(2);
    }

    @Test
    @WithMockCustomUser
    void getPersonalReservations() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("User")
                .build();
        userRepository.save(user);

        Table table = Table.builder()
                .minPeople(2)
                .maxPeople(3)
                .build();
        restaurant.addTable(table);
        tableRepository.save(table);

        Reservation reservation = Reservation.builder()
                .reservationTime(LocalDateTime.of(2025, 4, 1, 7, 45))
                .numPeople(2)
                .build();
        restaurant.addReservation(reservation);
        table.addReservation(reservation);
        user.addReservation(reservation);
        reservationRepository.save(reservation);

        reservation = Reservation.builder()
                .reservationTime(LocalDateTime.of(2025, 4, 2, 7, 45))
                .numPeople(2)
                .build();
        restaurant.addReservation(reservation);
        table.addReservation(reservation);
        user.addReservation(reservation);
        reservationRepository.save(reservation);

        Restaurant restaurant2 = Restaurant.builder()
                .name("Restaurant2")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant2 = restaurantRepository.save(restaurant2);

        SavorlyUser user2 = SavorlyUser.builder()
                .username("User2")
                .build();
        userRepository.save(user2);

        reservation = Reservation.builder()
                .reservationTime(LocalDateTime.of(2025, 4, 1, 7, 45))
                .numPeople(2)
                .build();
        restaurant2.addReservation(reservation);
        table.addReservation(reservation);
        user2.addReservation(reservation);
        reservationRepository.save(reservation);

        MvcResult actual = mockMvc.perform(get("/api/v1/restaurants/reservations")
                        .queryParam("username", "User")
                        .queryParam("date", "2025-04-01"))
                .andExpect(status().isOk())
                .andReturn();

        List<ReservationResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(1);
    }

    @Test
    @WithMockCustomUser
    void cancelOrder() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("User")
                .build();
        userRepository.save(user);

        Table table = Table.builder()
                .minPeople(2)
                .maxPeople(3)
                .build();
        restaurant.addTable(table);
        tableRepository.save(table);

        Reservation reservation = Reservation.builder()
                .reservationTime(LocalDateTime.of(2025, 4, 1, 7, 45))
                .numPeople(2)
                .build();
        restaurant.addReservation(reservation);
        table.addReservation(reservation);
        user.addReservation(reservation);
        reservation = reservationRepository.save(reservation);

        mockMvc.perform(delete("/api/v1/restaurants/reservations/{reservationId}", reservation.getId().toString())
                        .queryParam("username", "User")
                        .queryParam("date", "2025-04-01"))
                .andExpect(status().isOk());

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }
}