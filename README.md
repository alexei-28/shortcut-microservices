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
