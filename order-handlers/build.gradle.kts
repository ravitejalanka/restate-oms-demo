plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.serialization")
    id("io.spring.dependency-management")
    id("org.springframework.boot")
    id("com.google.devtools.ksp")
}

val restateVersion: String by rootProject.extra
val kotlinCoroutinesVersion: String by rootProject.extra
dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0")
    }
}

dependencies {
    implementation(project(":order-domain"))

    // Restate SDK
    implementation("dev.restate:sdk-spring-boot-kotlin-starter:${restateVersion}")
    ksp("dev.restate:sdk-api-kotlin-gen:${restateVersion}")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinCoroutinesVersion}")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")

    // Jackson for Restate SDK
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

springBoot {
    mainClass.set("com.learning.restate.with.boot.handlers.HandlersApplication")
}
