plugins {
    id("war")
}

version = "0.1"

dependencies {
    annotationProcessor(libs.lombok)

    providedCompile(libs.javaee.api)
    providedCompile(libs.jaxb.api)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)

    implementation(libs.lang3)
    implementation(libs.rxjava.core)
    implementation(libs.rxjava.reactivestreams)
    implementation(libs.slf4j.api)
    implementation(project(":grooves-example-test"))
    implementation(project(":grooves-java"))

    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(platform(libs.testcontainers.bom))

    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
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