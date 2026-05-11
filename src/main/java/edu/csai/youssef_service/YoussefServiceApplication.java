package edu.csai.youssef_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableAsync           // Required for @Async in WorkflowEventProducer
@EnableScheduling      // Good practice for background tasks
public class YoussefServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YoussefServiceApplication.class, args);
    }
}

