plugins {
    id("war")
    id("groovy")
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

    // Testcontainers support
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.spock)
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