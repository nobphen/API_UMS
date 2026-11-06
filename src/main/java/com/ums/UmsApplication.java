package com.ums;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Start point of the University Management System API. Scheduling is on for timed jobs (@Scheduled). */
@SpringBootApplication
@EnableScheduling
public class UmsApplication {

    /** Starts the Spring Boot app. */
    public static void main(String[] args) {
        SpringApplication.run(UmsApplication.class, args);
    }
}
