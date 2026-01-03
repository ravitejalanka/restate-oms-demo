plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

val restateVersion: String by rootProject.extra
dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0")
    }
}

dependencies {
    implementation(project(":order-domain"))
    implementation(project(":order-handlers"))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Restate Client SDK only
    implementation("dev.restate:sdk-kotlin-http:${restateVersion}")

    // Coroutines
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Jackson for JSON serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0")
}
springBoot {
    mainClass.set("com.learning.restate.with.boot.gateway.ApiGatewayApplication")
}