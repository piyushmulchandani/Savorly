package com.application.Savorly.config.interfaces;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('client_restaurant_admin', 'client_restaurant_worker', 'client_admin', 'client_user')")
public @interface hasAnyRole {
}
