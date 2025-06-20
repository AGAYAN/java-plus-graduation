package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.service.AnalyzeService;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum")
@ConfigurationPropertiesScan
@EnableJpaRepositories("ru.practicum.repository")
@EnableDiscoveryClient
@EntityScan("ru.practicum.model")
public class AnalyzerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApplication.class, args);
        AnalyzeService analyzer = context.getBean(AnalyzeService.class);
        analyzer.start();
    }
}
