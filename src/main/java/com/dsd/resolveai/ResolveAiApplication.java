package com.dsd.resolveai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ResolveAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResolveAiApplication.class, args);
    }

}
