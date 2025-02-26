package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class StatsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatsServiceApplication.class, args);
    }
}