package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.service.AggregatorService;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableDiscoveryClient
public class AggregatorApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorApplication.class, args);
        AggregatorService service = context.getBean(AggregatorService.class);
        service.startProcessing();
    }
}
