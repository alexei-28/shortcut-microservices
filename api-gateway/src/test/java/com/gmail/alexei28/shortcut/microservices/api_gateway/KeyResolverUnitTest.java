package com.gmail.alexei28.shortcut.microservices.api_gateway;

import com.gmail.alexei28.shortcut.microservices.api_gateway.config.RateLimiterConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

class KeyResolverUnitTest {

    private final RateLimiterConfig rateLimiterConfig = new RateLimiterConfig();

    @Test
    @DisplayName("Должен возвращать IP-адрес клиента, если remoteAddress присутствует")
    void shouldReturnHostAddressWhenRemoteAddressIsPresent() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .remoteAddress(new InetSocketAddress("192.168.1.50", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(rateLimiterConfig.ipKeyResolver().resolve(exchange))
                .expectNext("192.168.1.50")
                .verifyComplete();
    }

    @Test
    @DisplayName("Должен возвращать 'anonymous', если remoteAddress равен null")
    void shouldReturnAnonymousWhenRemoteAddressIsNull() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(rateLimiterConfig.ipKeyResolver().resolve(exchange))
                .expectNext("anonymous")
                .verifyComplete();
    }
}