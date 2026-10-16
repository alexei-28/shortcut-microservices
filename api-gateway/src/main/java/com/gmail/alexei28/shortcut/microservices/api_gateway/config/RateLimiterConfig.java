package com.gmail.alexei28.shortcut.microservices.api_gateway.config;

import com.gmail.alexei28.shortcut.microservices.api_gateway.ApiGatewayApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

/*
    В Spring Cloud Gateway фильтр RequestRateLimiter реализует ограничение количества запросов по алгоритму Token Bucket
    с использованием Redis.
    По умолчанию используется класс RedisRateLimiter, которому требуется Bean KeyResolver для определения ключа
    (IP-адрес, JWT-токен, ID пользователя или имя маршрута).
*/
@Configuration
public class RateLimiterConfig {
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterConfig.class);

    /*
        Чтобы Gateway понимал, к кому применять ограничение, объявляется KeyResolver.
        KeyResolver определяет, по какому признаку будет группироваться лимит (IP-адрес, JWT-токен, API-ключ или имя пользователя).
        В данном примере ограничение накладывается по IP-адресу клиента.
    */

    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            logger.info("ipKeyResolver, Headers: {}", exchange.getRequest().getHeaders());
            // 1. Проверяем заголовок X-Forwarded-For
            String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (StringUtils.hasText(xForwardedFor)) {
                // Заголовок может содержать список IP через запятую: "client, proxy1, proxy2"
                String ipAddress = xForwardedFor.split(",")[0].trim();
                return Mono.just(ipAddress);
            }

            // 2. Фолбэк на прямое сокетное соединение
            if (exchange.getRequest().getRemoteAddress() != null
                    && exchange.getRequest().getRemoteAddress().getAddress() != null) {
                return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
            }

            // 3. Фолбэк для анонимных вызовов
            return Mono.just("anonymous");
        };
    }

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            logger.info("apiKeyResolver, Headers: {}", exchange.getRequest().getHeaders());
            String apiKey = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-API-Key");

            if (StringUtils.hasText(apiKey)) {
                return Mono.just(apiKey);
            }
            return Mono.just("anonymous");
        };
    }
}