package com.mondial.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MondialApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MondialApiApplication.class, args);
    }
}

