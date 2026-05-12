package com.tumo;

import com.tumo.global.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TumoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TumoApplication.class, args);
    }

}
