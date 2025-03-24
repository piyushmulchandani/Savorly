package com.application.Savorly.domain.entity;

import com.application.Savorly.domain.catalog.SavorlyRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SavorlyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private SavorlyRole role;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastLogonDate;
}
