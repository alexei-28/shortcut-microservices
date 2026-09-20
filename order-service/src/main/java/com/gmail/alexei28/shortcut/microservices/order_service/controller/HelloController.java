package com.gmail.alexei28.shortcut.microservices.order_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

// http://localhost:8081/hello
@RestController
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/hello")
    public String sayHello() {
        logger.info("Hello, Order Service! Date: {}", LocalDateTime.now());
        return "Hello, Order Service! Date: "+ LocalDateTime.now();
    }
}