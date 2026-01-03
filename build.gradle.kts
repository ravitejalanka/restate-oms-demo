import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
    kotlin("plugin.serialization") version "2.2.21" apply false
    id("org.springframework.boot") version "4.0.0" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("com.google.devtools.ksp") version "2.2.21-2.0.4" apply false
}

allprojects {
    group = "com.learning.restate.with.boot"
    version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xannotation-default-target=param-property"
            )
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    configure<KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }
}

extra["restateVersion"] = "2.4.1"
extra["kotlinCoroutinesVersion"] = "1.10.2"
