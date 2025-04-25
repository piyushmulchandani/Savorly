package com.application.savorly.repository;

import com.application.savorly.domain.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, QuerydslPredicateExecutor<Restaurant> {

    Optional<Restaurant> findByName(String name);
}
