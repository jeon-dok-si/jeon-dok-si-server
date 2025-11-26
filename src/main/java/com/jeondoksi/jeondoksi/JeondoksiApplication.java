package com.jeondoksi.jeondoksi;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class JeondoksiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JeondoksiApplication.class, args);
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }
}
