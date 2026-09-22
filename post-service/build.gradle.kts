plugins {
    java
    id("org.springframework.boot") version "3.2.9"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.gmail.alexei28.shortcut.microservices.post_service"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val mapstructVersion = "1.6.3"
val openapiVersion = "2.6.0"

dependencies {
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${openapiVersion}")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.withType<Test> {
    useJUnitPlatform()
}