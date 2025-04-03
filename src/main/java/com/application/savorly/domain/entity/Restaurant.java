package com.application.savorly.domain.entity;

import com.application.savorly.domain.catalog.CuisineType;
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

    private boolean isPublic = false;

    private LocalTime openTime;

    private LocalTime closeTime;

    private String imageUrl;

    private String ownershipProofUrl;

    @Enumerated(EnumType.STRING)
    private CuisineType cuisineType;

    private String description;

    private String address;

    private String phone;

    private String city;

    private String country;

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.EAGER)
    private List<SavorlyUser> workers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Product> products = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Table> tables = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();
}
