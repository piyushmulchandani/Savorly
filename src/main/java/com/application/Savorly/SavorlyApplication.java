package com.application.Savorly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.application.Savorly")
public class SavorlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SavorlyApplication.class, args);
	}
}
