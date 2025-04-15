package com.application.savorly.repository;

import com.application.savorly.domain.entity.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TableRepository extends JpaRepository<Table, Long>, QuerydslPredicateExecutor<Table> {

    @Query("SELECT MAX(t.tableNumber) FROM Table t WHERE t.restaurant.id = :restaurantId")
    Integer findMaxTableNumberByRestaurantId(@Param("restaurantId") Long restaurantId);

    Optional<Table> findByTableNumberAndRestaurant_Id(Integer tableNumber, Long id);

}
