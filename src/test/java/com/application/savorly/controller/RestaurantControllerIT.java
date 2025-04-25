package com.application.savorly.controller;

import com.application.savorly.SavorlyApplication;
import com.application.savorly.config.interfaces.WithMockCustomUser;
import com.application.savorly.domain.catalog.CuisineType;
import com.application.savorly.domain.catalog.RestaurantStatus;
import com.application.savorly.domain.catalog.SavorlyRole;
import com.application.savorly.domain.entity.Reservation;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.domain.entity.Table;
import com.application.savorly.dto.create.RestaurantCreationDto;
import com.application.savorly.dto.modify.RestaurantModificationDto;
import com.application.savorly.dto.response.RestaurantResponseDto;
import com.application.savorly.repository.ReservationRepository;
import com.application.savorly.repository.RestaurantRepository;
import com.application.savorly.repository.TableRepository;
import com.application.savorly.repository.UserRepository;
import com.application.savorly.service.CloudinaryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(classes = {SavorlyApplication.class})
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:/application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class RestaurantControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private final LocalTime openTime = LocalTime.of(8, 0);
    private final LocalTime closeTime = LocalTime.of(21, 0);

    @Test
    @WithMockCustomUser(username = "NewAdmin")
    void createRestaurant() throws Exception {
        RestaurantCreationDto restaurantCreationDto = RestaurantCreationDto.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .cuisineType(CuisineType.AMERICAN)
                .address("Address")
                .city("Madrid")
                .country("Spain")
                .build();

        SavorlyUser user = SavorlyUser.builder()
                .username("NewAdmin")
                .build();
        user = userRepository.save(user);

        String dtoJson = objectMapper.writeValueAsString(restaurantCreationDto);

        MockMultipartFile restaurantPart = new MockMultipartFile(
                "restaurant", "", "application/json", dtoJson.getBytes()
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "file", "menu.pdf", "application/pdf", "Mock PDF content".getBytes()
        );

        when(cloudinaryService.uploadPdf(eq(filePart), any())).thenReturn("pdfUrl");

        mockMvc.perform(multipart("/api/v1/restaurants")
                        .file(restaurantPart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        List<Restaurant> restaurants = restaurantRepository.findAll();
        user = userRepository.findById(user.getId()).orElseThrow();
        List<Table> tables = tableRepository.findAll();

        assertThat(restaurants).hasSize(1);
        assertThat(restaurants.getFirst().getOwnershipProofUrl()).isNotNull();
        assertThat(tables).hasSize(1);
        assertThat(tables.getFirst().getTableNumber()).isZero();
        assertThat(user.getRestaurant().getId()).isEqualTo(restaurants.getFirst().getId());
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin", username = "RestaurantAdmin")
    void uploadImage() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("RestaurantAdmin")
                .build();
        restaurant.addWorker(user);
        userRepository.save(user);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "restaurant.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        when(cloudinaryService.uploadImage(eq(file), any())).thenReturn("pdfUrl");

        mockMvc.perform((multipart("/api/v1/restaurants/upload-image/{restaurantId}", restaurant.getId())
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)))
                .andExpect(status().isOk());

        restaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();

        assertThat(restaurant.getImageUrl()).isNotNull();
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin", username = "RestaurantAdmin")
    void uploadImage_isForbidden() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("RestaurantAdmin")
                .build();
        userRepository.save(user);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "restaurant.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        when(cloudinaryService.uploadImage(eq(file), any())).thenReturn("pdfUrl");

        mockMvc.perform((multipart("/api/v1/restaurants/upload-image/{restaurantId}", restaurant.getId())
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void acceptRestaurantRequest() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .creator("NewAdmin")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        mockMvc.perform((post("/api/v1/restaurants/accept/{restaurantId}", restaurant.getId())))
                .andExpect(status().isOk());

        restaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();

        assertThat(restaurant.getStatus()).isEqualTo(RestaurantStatus.PRIVATE);
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void acceptRestaurantRequest_BadRequest() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .creator("NewAdmin")
                .status(RestaurantStatus.PRIVATE)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        mockMvc.perform((post("/api/v1/restaurants/accept/{restaurantId}", restaurant.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void rejectRestaurantRequest() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .creator("NewAdmin")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        String reason = "Reason";
        mockMvc.perform((post("/api/v1/restaurants/reject/{restaurantId}", restaurant.getId())
                        .content(reason)
                        .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk());

        restaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();

        assertThat(restaurant.getStatus()).isEqualTo(RestaurantStatus.REJECTED);
        assertThat(restaurant.getRejectionMessage()).isEqualTo(reason);
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void rejectRestaurantRequest_BadRequest() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .creator("NewAdmin")
                .status(RestaurantStatus.PRIVATE)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        String reason = "Reason";
        mockMvc.perform((post("/api/v1/restaurants/reject/{restaurantId}", restaurant.getId())
                        .content(reason)
                        .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    void getRestaurants_Unfiltered() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .cuisineType(CuisineType.AMERICAN)
                .status(RestaurantStatus.PRIVATE)
                .city("Madrid")
                .build();
        restaurantRepository.save(restaurant);

        Restaurant restaurant1 = Restaurant.builder()
                .name("Restaurant1")
                .openTime(openTime)
                .closeTime(closeTime)
                .cuisineType(CuisineType.MEXICAN)
                .status(RestaurantStatus.PUBLIC)
                .city("Barcelona")
                .build();
        restaurantRepository.save(restaurant1);

        MvcResult actual = mockMvc.perform((get("/api/v1/restaurants")))
                .andExpect(status().isOk())
                .andReturn();

        List<RestaurantResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(2);
    }

    @Test
    @WithMockCustomUser
    void getRestaurants_Filtered() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .cuisineType(CuisineType.AMERICAN)
                .status(RestaurantStatus.PRIVATE)
                .city("Madrid")
                .build();
        restaurantRepository.save(restaurant);

        Restaurant restaurant1 = Restaurant.builder()
                .name("Restaurant1")
                .openTime(openTime)
                .closeTime(closeTime)
                .cuisineType(CuisineType.MEXICAN)
                .status(RestaurantStatus.PUBLIC)
                .city("Barcelona")
                .build();
        restaurantRepository.save(restaurant1);

        MvcResult actual = mockMvc.perform((get("/api/v1/restaurants")
                        .queryParam("name", "Restaurant")
                        .queryParam("status", RestaurantStatus.PUBLIC.toString())
                        .queryParam("cuisineType", CuisineType.MEXICAN.toString())
                        .queryParam("city", "Barcelona")))
                .andExpect(status().isOk())
                .andReturn();

        List<RestaurantResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(1);
    }

    @Test
    @WithMockCustomUser
    void getRestaurants_FilteredByReservation() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .cuisineType(CuisineType.AMERICAN)
                .status(RestaurantStatus.PRIVATE)
                .city("Madrid")
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
                .reservationTime(LocalDateTime.of(2025, 4, 1, 8, 0))
                .numPeople(2)
                .build();
        restaurant.addReservation(reservation);
        table.addReservation(reservation);
        user.addReservation(reservation);
        reservationRepository.save(reservation);

        Restaurant restaurant1 = Restaurant.builder()
                .name("Restaurant1")
                .openTime(openTime)
                .closeTime(closeTime)
                .cuisineType(CuisineType.MEXICAN)
                .status(RestaurantStatus.PUBLIC)
                .city("Barcelona")
                .build();
        restaurantRepository.save(restaurant1);

        MvcResult actual = mockMvc.perform((get("/api/v1/restaurants")
                        .queryParam("name", "Restaurant")
                        .queryParam("status", RestaurantStatus.PUBLIC.toString())
                        .queryParam("cuisineType", CuisineType.MEXICAN.toString())
                        .queryParam("city", "Barcelona")
                        .queryParam("dateTime", "2025-04-01T08:30:00")
                        .queryParam("numPeople", Integer.toString(3))))
                .andExpect(status().isOk())
                .andReturn();

        List<RestaurantResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).isEmpty();
    }

    @Test
    @WithMockCustomUser(role = "admin")
    void updateRestaurant() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .cuisineType(CuisineType.AMERICAN)
                .status(RestaurantStatus.PUBLIC)
                .city("Madrid")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        SavorlyUser user = SavorlyUser.builder()
                .username("Username")
                .role(SavorlyRole.ADMIN)
                .build();
        userRepository.save(user);

        RestaurantModificationDto restaurantModificationDto = RestaurantModificationDto.builder()
                .phone("12345")
                .status(RestaurantStatus.PRIVATE)
                .description("description")
                .address("address")
                .country("country")
                .build();

        MvcResult actual = mockMvc.perform(patch("/api/v1/restaurants/{restaurantId}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurantModificationDto)))
                .andExpect(status().isOk())
                .andReturn();

        Restaurant response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {});

        assertThat(response.getStatus()).isEqualTo(RestaurantStatus.PRIVATE);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin")
    void deleteRestaurant() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .openTime(openTime)
                .closeTime(closeTime)
                .cuisineType(CuisineType.AMERICAN)
                .status(RestaurantStatus.PUBLIC)
                .city("Madrid")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        Table table = Table.builder()
                .tableNumber(0)
                .build();
        restaurant.addTable(table);
        tableRepository.save(table);

        SavorlyUser user = SavorlyUser.builder()
                .username("Username")
                .role(SavorlyRole.RESTAURANT_ADMIN)
                .build();
        restaurant.addWorker(user);
        userRepository.save(user);

        mockMvc.perform(delete("/api/v1/restaurants/{restaurantId}", restaurant.getId()))
                .andExpect(status().isOk());

        assertThat(restaurantRepository.findAll()).isEmpty();
        assertThat(tableRepository.findAll()).isEmpty();
        assertThat(userRepository.findAll()).hasSize(1);
    }

}