package com.tumo;

import com.tumo.global.security.jwt.JwtProperties;
import com.tumo.stock.adapter.out.kis.config.KisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        JwtProperties.class,
        KisProperties.class
})
public class TumoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TumoApplication.class, args);
    }

}
