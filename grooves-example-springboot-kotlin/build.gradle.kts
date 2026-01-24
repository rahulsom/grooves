plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.spring")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.rxjava.reactivestreams)
    implementation(libs.spring.aspects)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    implementation(libs.spring.boot.starter.webflux)
    implementation(project(":grooves-api"))
    implementation(project(":grooves-example-test"))

    runtimeOnly(libs.activation)
    runtimeOnly(libs.jakarta.jaxb.api)
    runtimeOnly(libs.sunjaxb.impl)

    testImplementation(libs.reactor.test)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(platform(libs.testcontainers.bom))

    testRuntimeOnly(libs.junit.platform.commons)
    testRuntimeOnly(libs.junit.platform.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

val junit = libs.versions.junit
configurations.testRuntimeClasspath {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.junit.platform") {
                useVersion(junit.platform.get())
            }
            if (requested.group == "org.junit.jupiter") {
                useVersion(junit.core.get())
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    val isMacArm = System.getProperty("os.name").contains("Mac") && System.getProperty("os.arch") == "aarch64"
    if (isMacArm) {
        environment("DOCKER_HOST", "unix://${System.getenv("HOME")}/.docker/run/docker.sock")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}