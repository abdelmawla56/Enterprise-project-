package edu.csai.youssef_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class YoussefServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YoussefServiceApplication.class, args);
    }
}
