package com.application.Savorly.repository;

import com.application.Savorly.domain.entity.SavorlyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SavorlyUser, Long>, QuerydslPredicateExecutor<SavorlyUser> {
    Optional<SavorlyUser> findByUsernameIgnoreCase(String username);


}
