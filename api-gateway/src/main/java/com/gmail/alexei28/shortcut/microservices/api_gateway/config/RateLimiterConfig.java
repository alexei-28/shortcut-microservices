package com.gmail.alexei28.shortcut.microservices.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    /*
        Чтобы Gateway понимал, к кому применять ограничение, объявляется KeyResolver.
        KeyResolver определяет, по какому признаку будет группироваться лимит (IP-адрес, JWT-токен, API-ключ или имя пользователя).
        В данном примере ограничение накладывается по IP-адресу клиента.
    */

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
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
}