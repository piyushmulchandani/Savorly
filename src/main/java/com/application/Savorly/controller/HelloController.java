package com.application.Savorly.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HelloController {

    @GetMapping("/hello")
    @PreAuthorize("hasRole('client_user')")
    public String hello(
            @RequestParam String name
    ) {
        return "Hello, " + name + "!";
    }

    @GetMapping("/helloAdmin")
    @PreAuthorize("hasRole('client_admin')")
    public String helloAdmin(
            @RequestParam String name
    ) {
        return "Hello, Admin " + name + "!";
    }
}