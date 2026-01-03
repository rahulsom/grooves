plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.waena.published)
}

apply {
    from("$rootDir/gradle/jacoco.gradle")
}

description = "Types that are used by Grooves to build an Event Sourcing system"

dependencies {
    api(libs.reactive.streams)
    compileOnly(libs.kotlin.stdlib.jdk8)
    implementation(libs.slf4j.api)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}