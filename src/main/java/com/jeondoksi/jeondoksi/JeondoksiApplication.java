package com.jeondoksi.jeondoksi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class JeondoksiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JeondoksiApplication.class, args);
    }
}
