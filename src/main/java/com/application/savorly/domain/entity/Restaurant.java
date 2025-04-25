package com.application.savorly.domain.entity;

import com.application.savorly.domain.catalog.CuisineType;
import com.application.savorly.domain.catalog.RestaurantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.REQUESTED;

    private LocalTime openTime;

    private LocalTime closeTime;

    @Enumerated(EnumType.STRING)
    private CuisineType cuisineType;

    private String description;

    private String address;

    private String phone;

    private String city;

    private String country;

    private String creator;

    private String imageUrl;

    private String ownershipProofUrl;

    private String rejectionMessage;

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY)
    private List<SavorlyUser> workers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Table> tables = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    public void addWorker(SavorlyUser user) {
        workers.add(user);
        user.setRestaurant(this);
    }

    public void addProduct(Product product) {
        products.add(product);
        product.setRestaurant(this);
    }

    public void addTable(Table table) {
        tables.add(table);
        table.setRestaurant(this);
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setRestaurant(this);
    }
}
