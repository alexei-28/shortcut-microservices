plugins {
    java
    id("org.springframework.boot") version "3.2.9"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.gmail.alexei28.shortcut.microservices"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Define the Spring Cloud version (e.g., 2023.0.x for Spring Boot 3.2.x)
extra["springCloudVersion"] = "2023.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Manages Spring Cloud dependency versions automatically
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}