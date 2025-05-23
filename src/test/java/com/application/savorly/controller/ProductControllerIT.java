package com.application.savorly.controller;

import com.application.savorly.SavorlyApplication;
import com.application.savorly.config.interfaces.WithMockCustomUser;
import com.application.savorly.domain.catalog.ProductCategory;
import com.application.savorly.domain.catalog.SavorlyRole;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.Restaurant;
import com.application.savorly.domain.entity.SavorlyUser;
import com.application.savorly.dto.create.ProductCreationDto;
import com.application.savorly.dto.response.ProductResponseDto;
import com.application.savorly.repository.ProductRepository;
import com.application.savorly.repository.RestaurantRepository;
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
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    @WithMockCustomUser(role = "restaurant_admin")
    void addProduct() throws Exception {
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

        ProductCreationDto productCreationDto = ProductCreationDto.builder()
                .restaurantId(restaurant.getId())
                .name("product")
                .price(BigDecimal.TEN)
                .category(ProductCategory.DESSERT)
                .build();

        mockMvc.perform(post("/api/v1/restaurants/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productCreationDto)))
                .andExpect(status().isOk());

        List<Product> savedProducts = productRepository.findAll();

        assertThat(savedProducts).hasSize(1);

        assertThat(savedProducts.getFirst().getName()).isEqualTo("product");
        assertThat(savedProducts.getFirst().getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(savedProducts.getFirst().getCategory()).isEqualTo(ProductCategory.DESSERT);
        assertThat(savedProducts.getFirst().getRestaurant()).isEqualTo(restaurant);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin")
    void addProduct_AlreadyExists() throws Exception {
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

        Product product = Product.builder()
                .name("product")
                .build();
        restaurant.addProduct(product);
        productRepository.save(product);

        ProductCreationDto productCreationDto = ProductCreationDto.builder()
                .restaurantId(restaurant.getId())
                .name("product")
                .price(BigDecimal.TEN)
                .category(ProductCategory.DESSERT)
                .build();

        mockMvc.perform(post("/api/v1/restaurants/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productCreationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    void getRestaurantProducts_Unfiltered() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        Product product1 = Product.builder()
                .name("product1")
                .category(ProductCategory.MAIN_COURSE)
                .price(BigDecimal.TEN)
                .build();
        restaurant.addProduct(product1);
        Product product2 = Product.builder()
                .name("product2")
                .category(ProductCategory.MAIN_COURSE)
                .price(BigDecimal.valueOf(10.99))
                .build();
        restaurant.addProduct(product2);

        productRepository.saveAll(List.of(product1, product2));

        MvcResult actual = mockMvc.perform(get("/api/v1/restaurants/products")
                        .queryParam("restaurantId", restaurant.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        List<ProductResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(2);
        assertThat(response.getFirst().getName()).isEqualTo("product1");
        assertThat(response.getFirst().getCategory()).isEqualTo(ProductCategory.MAIN_COURSE);
        assertThat(response.getFirst().getPrice()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @WithMockCustomUser
    void getRestaurantProducts_Filtered() throws Exception {
        Restaurant restaurant = Restaurant.builder()
                .name("Restaurant")
                .build();
        restaurant = restaurantRepository.save(restaurant);

        Product product1 = Product.builder()
                .name("product1")
                .category(ProductCategory.MAIN_COURSE)
                .price(BigDecimal.TEN)
                .build();
        restaurant.addProduct(product1);
        Product product2 = Product.builder()
                .name("product2")
                .category(ProductCategory.MAIN_COURSE)
                .price(BigDecimal.valueOf(10.99))
                .build();
        restaurant.addProduct(product2);

        productRepository.saveAll(List.of(product1, product2));

        MvcResult actual = mockMvc.perform(get("/api/v1/restaurants/products")
                        .queryParam("restaurantId", restaurant.getId().toString())
                        .queryParam("category", ProductCategory.MAIN_COURSE.name())
                        .queryParam("name", "product2"))
                .andExpect(status().isOk())
                .andReturn();

        List<ProductResponseDto> response = objectMapper.readValue(actual.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(response).hasSize(1);
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin")
    void deleteProduct() throws Exception {
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

        Product product = Product.builder()
                .name("product")
                .category(ProductCategory.MAIN_COURSE)
                .price(BigDecimal.TEN)
                .build();
        restaurant.addProduct(product);
        product = productRepository.save(product);

        mockMvc.perform(delete("/api/v1/restaurants/products/{productId}", product.getId()))
                .andExpect(status().isOk());

        assertThat(productRepository.existsById(product.getId())).isFalse();
    }

    @Test
    @WithMockCustomUser(role = "restaurant_admin")
    void deleteProduct_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/restaurants/products/{productId}", 1L))
                .andExpect(status().isNotFound());
    }
}