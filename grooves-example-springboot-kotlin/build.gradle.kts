import com.sourcemuse.gradle.plugin.GradleMongoPluginExtension

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.spring)
    id("groovy")
    alias(libs.plugins.mongo)
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlin.reflect)
    implementation(libs.rxjava.reactivestreams)
    implementation("org.springframework:spring-aspects")
    implementation(project(":grooves-api"))
    implementation(project(":grooves-example-test"))

    runtimeOnly(libs.jaxb.api)
    runtimeOnly(libs.sunjaxb.core)
    runtimeOnly(libs.sunjaxb.impl)
    runtimeOnly(libs.activation)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
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