package com.application.savorly.repository;

import com.application.savorly.domain.entity.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TableRepository extends JpaRepository<Table, Long>, QuerydslPredicateExecutor<Table> {
}
