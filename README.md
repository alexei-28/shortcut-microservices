# shortcut-microservices
Shortcut Microservices test project

Repo: https://github.com/alexei-28/shortcut-microservices

Mentor platform - Shortcut: https://shortcut.education/

---
* **Environment**
    * Java 21
    * Gradle: 8.5
    * Spring Boot: 3.2.9
    * Test tools:
        * JUnit 5
        * Testcontainers
   
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
    * Prometheus - http://localhost:9090
    * Grafana - http://localhost:3000
    * * Username: admin@localhost
    * * Password: your_secure_password
    * Топ-5 наиболее популярных и функциональных готовых дашбордов Grafana для мониторинга приложений на Spring Boot (через Micrometer и Prometheus)
  
    |---+--------------------------------+--------------+-------------------------------------------------------------------------------------|
    | # | Название дашборда              | ID в Grafana | Назначение и ключевые метрики                                                       |
    |---+--------------------------------+--------------+-------------------------------------------------------------------------------------|
    | 1 | JVM (Micrometer)               |         4701 | Базовый стандарт. Детальный мониторинг JVM: Heap/Non-Heap память, .                 |
    |   |                                |              | Garbage Collection (GC), активные потоки (Threads), CPU использование               |
    |---+--------------------------------+--------------+-------------------------------------------------------------------------------------|
    | 2 | Spring Boot 3.x Statistics     |        19004 | Для Spring Boot 3+. Отслеживание HTTP-запросов (RPS, Latency,                       |
    |   |                                |              | коды ответов 2xx/4xx/5xx), соединения HikariCP, работы Tomcat и метрик JVM.         |
    |---+--------------------------------+--------------+-------------------------------------------------------------------------------------|
    | 3 | Spring Boot 2.1 System Monitor |        11378 | Универсальный APM. Удобная визуализация суммарного состояния системы:               |
    |   |                                |              | RPS, время отклика, использование пула соединений БД и кучи JVM.                    |
    |---+--------------------------------+--------------+-------------------------------------------------------------------------------------|
    | 4 | SpringBoot APM Dashboard       |        12900 | Для Kubernetes & Microservices. Удобен при деплое микросервисов в K8s.              |
    |   |                                |              | Содержит удобные фильтры по нодам, неймспейсам и конкретным инстансам.              |
    |---+--------------------------------+--------------+-------------------------------------------------------------------------------------|
    | 5 | Spring Boot Statistics         |         6756 | Классический дашборд. Охватывает метрики Actuator:                                  |
    |   |                                |              | пулы соединений HikariCP, статистика REST-контроллеров, Uptime и системные ресурсы. |
    |---+--------------------------------+--------------+-------------------------------------------------------------------------------------|

   
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
