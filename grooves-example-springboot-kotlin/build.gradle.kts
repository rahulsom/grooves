plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.spring")
    id("groovy")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.rxjava.reactivestreams)
    implementation("org.springframework:spring-aspects")
    implementation(project(":grooves-api"))
    implementation(project(":grooves-example-test"))

    runtimeOnly(libs.jaxb.api)
    runtimeOnly(libs.sunjaxb.core)
    runtimeOnly(libs.sunjaxb.impl)
    runtimeOnly(libs.activation)

    testImplementation(platform(libs.testcontainers.bom))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.spock)
    testImplementation(libs.spock.core)
    testImplementation(libs.spock.spring)
    testImplementation("io.projectreactor.addons:reactor-test:3.0.7.RELEASE")
    testRuntimeOnly(libs.junit.platform.launcher)
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