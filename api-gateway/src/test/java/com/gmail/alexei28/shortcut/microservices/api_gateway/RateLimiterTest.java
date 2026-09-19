package com.gmail.alexei28.shortcut.microservices.api_gateway;

import com.gmail.alexei28.shortcut.microservices.api_gateway.config.RateLimiterConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import({RateLimiterConfig.class, RateLimiterTest.TestRouteConfig.class})
class RateLimiterTest {
    private static final int RATE_LIMITER_REPLENISH_RATE = 1;
    private static final int RATE_LIMITER_BURST_CAPACITY = 3;
    private static final int REQUESTED_TOKENS = 1;
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // Подключаем Spring Data Redis к Testcontainers
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        // Жестко задаем тестовые лимиты независимо от prod-конфигурации
        // Сколько токенов добавляется в ведро за 1 секунду (Refill Rate)
        registry.add("test.rate-limiter.replenishRate", () -> RATE_LIMITER_REPLENISH_RATE);
        // Максимальная емкость ведра (Capacity), определяющая допустимый размер мгновенного всплеска
        registry.add("test.rate-limiter.burstCapacity", () -> RATE_LIMITER_BURST_CAPACITY);
        // Сколько токенов списывается за 1 HTTP-запрос (по умолчанию 1)
        registry.add("test.rate-limiter.requestedTokens", () -> REQUESTED_TOKENS);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Должен пропускать запросы в пределах burstCapacity и возвращать 429 при превышении")
    void shouldRateLimitByIpAddress() {
        String clientIp = "192.168.1.100";

        // Первые 3 запроса (согласно burstCapacity = 3) должны пройти успешно
        for (int i = 0; i < RATE_LIMITER_BURST_CAPACITY; i++) {
            webTestClient.get()
                    .uri("/test-limit")
                    .header("X-Forwarded-For", clientIp)
                    .exchange()
                    .expectStatus().isOk();
        }

        // 4-й запрос превышает burstCapacity -> получаем 429 Too Many Requests
        webTestClient.get()
                .uri("/test-limit")
                .header("X-Forwarded-For", clientIp)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("Запросы с разных IP не должны влиять на лимиты друг друга")
    void shouldIsolateLimitsForDifferentIps() {
        String ipA = "10.0.0.1";
        String ipB = "10.0.0.2";

        // Исчерпываем лимит для IP A
        for (int i = 0; i < RATE_LIMITER_BURST_CAPACITY; i++) {
            webTestClient.get().uri("/test-limit").header("X-Forwarded-For", ipA).exchange().expectStatus()
                    .isOk();
        }
        webTestClient.get().uri("/test-limit").header("X-Forwarded-For", ipA).exchange().expectStatus()
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        // IP B все еще имеет полный бакет на 3 запроса
        webTestClient.get().uri("/test-limit").header("X-Forwarded-For", ipB).exchange().expectStatus()
                .isOk();
    }

    @TestConfiguration
    static class TestRouteConfig {

        @Bean
        public RouteLocator testRoutes(RouteLocatorBuilder builder, RedisRateLimiter redisRateLimiter, KeyResolver ipKeyResolver) {
            return builder.routes()
                    .route("test_rate_limited_route", r -> r
                            .path("/test-limit")
                            .filters(f -> f
                                    .requestRateLimiter(config -> config
                                            .setRateLimiter(redisRateLimiter)
                                            .setKeyResolver(ipKeyResolver)
                                    )
                                    .setStatus(HttpStatus.OK.value())
                            )
                            .uri("no-op://loopback")
                    )
                    .build();
        }

        @Bean
        public RedisRateLimiter customRedisRateLimiter(
                @Value("${test.rate-limiter.replenishRate}") int replenishRate,
                @Value("${test.rate-limiter.burstCapacity}") int burstCapacity,
                @Value("${test.rate-limiter.requestedTokens}") int requestedTokens) {
            return new RedisRateLimiter(replenishRate, burstCapacity, requestedTokens);
        }
    }
}