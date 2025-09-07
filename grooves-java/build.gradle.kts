plugins {
    id("java-library")
    id("com.github.rahulsom.waena.published")
}

apply { from("$rootDir/gradle/jacoco.gradle") }

description = "AspectJ Support for Java implementations of Grooves"

dependencies {
    api(project(":grooves-api"))

    implementation(libs.google.autoservice)

    compileOnly(libs.jetbrains.annotations)

    testImplementation(libs.google.compiletesting)
    testImplementation(libs.rxjava.core)
    testImplementation(libs.rxjava.reactivestreams)

    testRuntimeOnly(libs.jetbrains.annotations)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}