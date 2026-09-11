package com.gmail.alexei28.shortcut.microservices.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        // Лимит по IP-адресу клиента
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "anonymous"
        );

        // Альтернатива: лимит по Заголовку/JWT (например, Authorization или X-User-Id)
        // return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
        //                        .defaultIfEmpty("anonymous");
    }
}