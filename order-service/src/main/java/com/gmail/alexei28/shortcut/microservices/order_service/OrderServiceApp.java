package com.gmail.alexei28.shortcut.microservices.order_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApp {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceApp.class);

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApp.class, args);

        logger.info("\n\n ===== Application started successfully! =====\nOrder Service");
        logger.info(
                "\nJava version: {}, Java vendor: {}",
                System.getProperty("java.version"),
                System.getProperty("java.vendor"));
    }

}