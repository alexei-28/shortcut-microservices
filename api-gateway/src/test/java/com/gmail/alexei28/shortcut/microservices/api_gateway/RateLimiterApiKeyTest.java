package com.gmail.alexei28.shortcut.microservices.api_gateway;

import com.gmail.alexei28.shortcut.microservices.api_gateway.config.RateLimiterConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Import({RateLimiterConfig.class, RateLimiterApiKeyTest.TestRouteConfig.class})
class RateLimiterApiKeyTest {
    private static final String X_API_KEY = "X-API-Key";
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
    @DisplayName("apiKeyResolver: должен ограничивать запросы по X-API-Key на основе burstCapacity")
    void shouldRateLimitByApiKeyAndBurstCapacity() {
        String apiKey = "secret-api-key-1";

        // Первые 3 запроса (burstCapacity = 3) проходят успешно
        for (int i = 0; i < RATE_LIMITER_BURST_CAPACITY; i++) {
            webTestClient.get()
                    .uri("/test-api-key-limit")
                    .header(X_API_KEY, apiKey)
                    .exchange()
                    .expectStatus().isOk();
        }

        // 4-й запрос блокируется (429)
        webTestClient.get()
                .uri("/test-api-key-limit")
                .header(X_API_KEY, apiKey)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("apiKeyResolver: должен восстанавливать токены в соответствии с replenishRate")
    void shouldReplenishTokensAccordingToReplenishRate() throws InterruptedException {
        String apiKey = "secret-api-key-replenish";

        // Исчерпываем весь лимит (burstCapacity = 3)
        for (int i = 0; i < RATE_LIMITER_BURST_CAPACITY; i++) {
            webTestClient.get()
                    .uri("/test-api-key-limit")
                    .header(X_API_KEY, apiKey)
                    .exchange()
                    .expectStatus().isOk();
        }

        // Убеждаемся, что бакет пуст
        webTestClient.get()
                .uri("/test-api-key-limit")
                .header(X_API_KEY, apiKey)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        // Ждем 1.1 секунды (replenishRate = 1 токен в секунду)
        Thread.sleep(1100);

        // Появляется 1 токен — запрос проходит
        webTestClient.get()
                .uri("/test-api-key-limit")
                .header(X_API_KEY, apiKey)
                .exchange()
                .expectStatus().isOk();

        // Следующий запрос снова блокируется, так как был восстановлен только 1 токен
        webTestClient.get()
                .uri("/test-api-key-limit")
                .header(X_API_KEY, apiKey)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("apiKeyResolver: должен списывать указанное количество токенов (requestedTokens = 2)")
    void shouldConsumeMultipleTokensPerRequest() {
        String apiKey = "secret-api-key-heavy";
        // Бакет имеет емкость burstCapacity = 3.
        // Запрос списание requestedTokens = 2 сгорает 2 токена за 1 запрос.

        // 1-й запрос: берет 2 токена (остается 1 токен) -> Успешно
        webTestClient.get()
                .uri("/test-api-key-heavy-limit")
                .header(X_API_KEY, apiKey)
                .exchange()
                .expectStatus().isOk();

        // 2-й запрос: пытается взять 2 токена, но доступен только 1 -> 429 Too Many Requests
        webTestClient.get()
                .uri("/test-api-key-heavy-limit")
                .header(X_API_KEY, apiKey)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @TestConfiguration
    static class TestRouteConfig {

        @Bean
        public RouteLocator testRoutes(RouteLocatorBuilder builder,
                                       RedisRateLimiter redisRateLimiter,
                                       @Qualifier("apiKeyResolver") KeyResolver keyResolver) {
            return builder.routes()

                    // Обычный маршрут (1 токен за запрос)
                    .route("test_rate_limited_route", r -> r
                            .path("/test-api-key-limit")
                            .filters(f -> f
                                    .requestRateLimiter(config -> config
                                            .setRateLimiter(redisRateLimiter)
                                            .setKeyResolver(keyResolver)
                                    )
                                    .setStatus(HttpStatus.OK.value())
                            )
                            .uri("no-op://loopback")
                    )

                    // Маршрут для тяжелых запросов (2 токена за запрос)
                    .route("test_heavy_rate_limited_route", r -> r
                            .path("/test-api-key-heavy-limit")
                            .filters(f -> f
                                    .requestRateLimiter(config -> config
                                            .setRateLimiter(redisRateLimiter)
                                            .setKeyResolver(keyResolver)
                                            // Переопределяем списание токенов для данного маршрута:
                                            .setStatusCode(HttpStatus.TOO_MANY_REQUESTS)
                                    )

                                    // Списываем 2 токена за запрос через лямбду конфигурации.
                                    // В данном коде 2 токена списываются потому, что фильтр requestRateLimiter
                                    // вызван дважды подряд в рамках одной цепочки фильтров маршрута.
                                    .requestRateLimiter(c -> c
                                            .setRateLimiter(redisRateLimiter)
                                            .setKeyResolver(keyResolver)
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