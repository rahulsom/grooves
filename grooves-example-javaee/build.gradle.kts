plugins {
    id("war")
}

version = "0.1"

dependencies {
    annotationProcessor(libs.lombok)

    providedCompile(libs.javaee.api)
    providedCompile(libs.jaxb.api)

    compileOnly(libs.lombok)
    compileOnly(libs.jetbrains.annotations)

    implementation(project(":grooves-java"))
    implementation(project(":grooves-example-test"))
    implementation(libs.slf4j.api)
    implementation(libs.lang3)
    implementation(libs.rxjava.core)
    implementation(libs.rxjava.reactivestreams)

    // JUnit 5 support
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Testcontainers support
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.junit.jupiter)
}

tasks.test {
    dependsOn("war")
    useJUnitPlatform()
    systemProperty("war.file.path", tasks.war.get().archiveFile.get().asFile.absolutePath)

    // Docker configuration for Mac ARM
    val isMacArm = System.getProperty("os.name").contains("Mac") && System.getProperty("os.arch") == "aarch64"
    if (isMacArm) {
        environment("DOCKER_HOST", "unix://${System.getenv("HOME")}/.docker/run/docker.sock")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}