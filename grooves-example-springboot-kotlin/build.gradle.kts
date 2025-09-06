import com.sourcemuse.gradle.plugin.GradleMongoPluginExtension

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.spring")
    id("groovy")
    id("com.sourcemuse.mongo")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlin.reflect)
    implementation(libs.rxjava.reactivestreams)
    implementation(libs.spring.aspects)
    implementation(project(":grooves-api"))
    implementation(project(":grooves-example-test"))

    runtimeOnly(libs.jaxb.api)
    runtimeOnly(libs.sunjaxb.core)
    runtimeOnly(libs.sunjaxb.impl)
    runtimeOnly(libs.activation)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spock.core)
    testImplementation(libs.spock.spring)
    testImplementation(libs.reactor.test)
}

tasks.named("bootRun") { dependsOn("startMongoDb") }
tasks.named("bootRun") { finalizedBy("stopMongoDb") }
tasks.named("test") { dependsOn("startMongoDb") }
tasks.named("test") { finalizedBy("stopMongoDb") }

configure<GradleMongoPluginExtension> {
    setPort(27021)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}