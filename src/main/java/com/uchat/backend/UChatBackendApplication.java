package com.uchat.backend;

import com.uchat.backend.config.UChatProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(UChatProperties.class)
public class UChatBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UChatBackendApplication.class, args);
    }
}
