# shortcut-microservices
Shortcut Microservices test project

Repo: https://github.com/alexei-28/shortcut-microservices

Mentor platform - Shortcut: https://shortcut.education/
---
* **Environment**
* All services run in separate Docker containers
* Infrastructured services
  * ELK (стек для сбора, хранения, поиска и визуализации логов)
  * Jaeger (Позволяет видеть путь одного HTTP-запроса через несколько сервисов)
  * Prometheus (Metrics)
  * Grafana (Dashboards)
  * Vault (secrets)
  * Redis (NoSQL)
  * Vault (secrets)
* My services
  * api-gateway
  * post-service
* Other
  * Java 21
  * Gradle: 8.5
  * Spring Boot: 3.2.9
  * REST API
  * GraphQL (client specifies exactly what data it needs)
  * Test tools:
    * JUnit 5
    * Testcontainers

* Как разблокировать Vault
* Run ./start-my-services-docker.sh  
  Automate unseal vault and restart my services.
* Or do all steps manually
  In root folder Create file /vault/secrets/unseal-key.txt and add unseal key. Must be in .gitignore.

  Если при первоначальной инициализации ты сохранил unseal keys, используй один из них:

  docker compose exec vault vault operator unseal
* Проверь, что Vault разблокирован

  docker compose exec vault vault status

* Чтобы полностью пересоздать все контейнеры (с пересборкой образов и применением новых изменений в конфигурации или коде), 
* выполните следующие команды в терминале из папки с вашим docker-compose.yml:

docker compose down

docker compose up --build -d

* Unseal Vault
* * Unseal Vault using the unseal key generated in the previous step:

  docker exec -it vault vault operator unseal <your-unseal-key>

*  After unseal vault you need to restart api-gateway and post-service

    docker compose restart api-gateway

    docker compose restart post-service

* Собери и запусти Docker контейнеры 

Из корневого каталога shortcut-microservices:
1. Проверить Compose на ошибки

     docker compose config

2. Собрать два приложения

   docker compose build api-gateway post-service

3. Запустить все Docker контейнеры

   docker compose up -d

4. Проверить состояние контейнеров

   docker compose ps

5. Посмотреть логи приложений 

   docker compose logs -f api-gateway

   docker compose logs -f post-service
   
   Health check api-gateway: http://localhost:8080/actuator/health

   Health check post-service: http://localhost:8081/actuator/health

6. Restart API Gateway (optional)

   docker compose restart api-gateway

7. Restart Post Service(optional)

   docker compose restart post-service

8. Проверь работоспособность
   После запуска:
   - api-gateway: http://localhost:8080/actuator/health
   - post-service: http://localhost:8081/actuator/health
   - Gateway Actuator: http://localhost:8080/actuator/health
   - Post Service Actuator: http://localhost:8081/actuator/health
   - GraphiQL: http://localhost:8080/graphiql — если соответствующий интерфейс включён и путь настроен именно так.
   
* **Install application environment**
    * Вход в веб-интерфейс (Redis)
    * Откройте браузер и перейдите по адресу: http://localhost:5540
  
      Проверка: В браузере должна загрузиться стартовая страница Redis Insight.
    * Подключение к базе данных:
      
      В интерфейсе нажмите Add Redis Database и заполните параметры подключения:
  
      Host: redis (имя сервиса из docker-compose)
  
      Port: 6379
      
      Database Alias: My Local Redis
      
      Password: my_master_password
      
      Проверка: Нажмите кнопку Test Connection — должно появиться подтверждение успеха, после чего нажмите Add Redis Database.
  
    * Run application: ./gradlew bootRun
    * Validate is application is up: http://localhost:8080/hello
    * ELK (Elasticsearch, Logstash, Kibana)
    * Prometheus - http://localhost:9090
    * Grafana - http://localhost:3000
    * * Username: admin@localhost
    * * Password: your_secure_password
    * Jaeger - http://localhost:16686
    * Kibana - http://localhost:5601
    * Vault UI - http://localhost:8200
    * * Unseal Key Portion - Enter N times "Unseal Key x"
    * Топ-5 наиболее популярных и функциональных готовых дашбордов Grafana для мониторинга приложений на Spring Boot (через Micrometer и Prometheus)

  | # | Dashboard Name                  | Grafana ID | Description |
  |---|----------------------------------|-----------:|-------------|
  | 1 | JVM (Micrometer)                | 4701 | General-purpose dashboard. Monitors JVM metrics: Heap/Non-Heap memory, Garbage Collection (GC), Threads, CPU utilization. |
  | 2 | Spring Boot 3.x Statistics      | 19004 | For Spring Boot 3+. Monitors HTTP metrics (RPS, Latency, 2xx/4xx/5xx status codes), HikariCP, Tomcat, and JVM metrics. |
  | 3 | Spring Boot 2.1 System Monitor  | 11378 | General APM dashboard. Provides application-level metrics such as RPS, response time, database connection pool, and JVM metrics. |
  | 4 | SpringBoot APM Dashboard        | 12900 | Designed for Kubernetes & Microservices. Provides monitoring for applications running in K8s, including request rates, latency, and application metrics. |
  | 5 | Spring Boot Statistics           | 6756 | General-purpose dashboard. Uses Actuator metrics: HikariCP connection pool, REST requests, uptime, and application metrics. |
  
    
   

---
* **ДЗ**
  * Изучить материалы по систем-дизайну из репозитория ментора:
    https://github.com/AlohaJava/system-design-interview-chamkin.
  * Создать проект: Spring Cloud Gateway + произвольный микросервис + rate limiting на Redis, 
    подключить ELK, Jaeger, Prometheus, Grafana, Vault для секретов. 
  * Изучить стратегии rate limiting: token bucket, leaky bucket, fixed window, sliding window.
  * Изучить конфигурации Redis для отказоустойчивости: Sentinel, кластер (3+3).
  * Изучить Feign клиенты в Spring.
  * Изучить GraphQL.
  * Изучить стратегии деплоймента: blue-green, canary, rolling update, recreate.
  * После выполнения первого ДЗ поднять Minikube (3-4 ноды) и развернуть созданный проект в Kubernetes.

# Решение
Почему для Gateway rate limiting на Redis?
Потому что Redis хорошо подходит для хранения общего состояния rate limiter-а, особенно когда Gateway работает в нескольких экземплярах.

Типичная архитектура:

                  ┌───────────────┐
                  │     Redis     │
                  │ rate limits   │
                  └───────┬───────┘
                          │
             ┌────────────┼────────────┐
             │            │            │
             v            v            v
        Gateway #1   Gateway #2   Gateway #3
             │            │            │
             └────────────┼────────────┘
                          v
                    Microservices

Именно поэтому в Spring Cloud Gateway часто используют Redis для rate limiting: не потому, что без Redis rate limiting невозможен, 
а потому что Redis даёт быстрое, общее для всех Gateway и атомарное хранилище состояния.

В Spring Cloud Gateway фильтр RequestRateLimiter реализует ограничение количества запросов по алгоритму Token Bucket с использованием Redis.
При превышении лимита Spring Cloud Gateway автоматически возвращает клиенту статус HTTP 429 Too Many Requests.

В shortcut-microservices сейчас фактически можно показать два разных observability-потока:

                 ┌──────────────────────┐
                 │   Spring Boot app    │
                 │   api-gateway        │
                 │   post-service       │
                 └──────────┬───────────┘
                            │
             ┌──────────────┴──────────────┐
             │                             │
            Logs                         Traces
             │                             │
             ▼                             ▼
        Logstash                         OTel
             │                             │
             ▼                             ▼
       Elasticsearch                    Jaeger
             │                             │
             ▼                             ▼
          Kibana                        Jaeger UI


### ELK flow:

    Spring Boot
         │
         │ JSON logs
         ▼
    Logstash :5000
         │
         │ parse / enrich
         ▼
    Elasticsearch :9200
         │
         ▼
     Kibana :5601

ELK отвечает в основном на вопрос "что произошло в логах?"

### Jaeger flow:

    Trace
    │
    ├── GET /post/123
    │      120 ms
    │
    ├────── PostController
    │        115 ms
    │
    ├──────── PostService.getPostById()
    │          110 ms
    │
    └────────── PostgreSQL
                95 ms

Jaeger позволяет увидеть весь путь одного запроса.

### Главное различие

| ELK                 | Jaeger                        |
| ------------------- | ----------------------------- |
| Logs                | Distributed tracing           |
| Что произошло?      | Где и сколько времени заняло? |
| ERROR / INFO / WARN | Trace / Span                  |
| Поиск событий       | Поиск запросов                |
| Elasticsearch       | Jaeger storage                |
| Kibana              | Jaeger UI                     |


# Как Spring Cloud Vault использует application name
Examples:

For post-service:

    spring.application.name=post-service
             │
             ▼
            Vault
             │
             ▼
            secret/post-service

For api-gateway:

    spring.application.name=api-gateway
             │
             ▼
        secret/api-gateway


Это очень удобно для архитектуры:
    
    Vault
    │
    ├── secret/
    │   ├── api-gateway
    │   │   └── redis credentials
    │   │
    │   └── post-service
    │       └── PostgreSQL credentials
    │
    └── ...
