plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}
val kotlinCoroutinesVersion: String by rootProject.extra
dependencies {
    // KotlinX Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinCoroutinesVersion}")
}
