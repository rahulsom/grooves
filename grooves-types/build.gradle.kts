plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    id("com.github.rahulsom.waena.published")
}

apply {
    from("$rootDir/gradle/jacoco.gradle")
}

description = "Types that are used by Grooves to build an Event Sourcing system"

dependencies {
    api(libs.reactive.streams)
    implementation(libs.slf4j.api)

    compileOnly(libs.kotlin.stdlib.jdk8)

    testImplementation(libs.spock.core)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}