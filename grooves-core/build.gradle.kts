plugins {
    alias(libs.plugins.logging.capabilities)
    alias(libs.plugins.post.compile.weaving)
    alias(libs.plugins.waena.published)
    id("java-library")
}

loggingCapabilities {
    enforceLogback("testRuntimeClasspath")
}

description = "Simpler implementation of Grooves"

dependencies {
    annotationProcessor(libs.lombok)

    compileOnly(libs.lombok)

    implementation(libs.aspectj.rt)
    implementation(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)

    testAnnotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)

    testImplementation(libs.assertj.core)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)

    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.log4j.core)
    testRuntimeOnly(libs.log4j.slf4j)
    testRuntimeOnly(libs.logback.classic)
    testRuntimeOnly(libs.slf4j.simple)
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Configure Slf4j Simple Logger
    systemProperty("org.slf4j.simpleLogger.log.com.github.rahulsom.grooves", "DEBUG")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}
