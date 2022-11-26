plugins {
    id("java-library")
    id("com.github.rahulsom.waena.published")
}

apply { from("$rootDir/gradle/jacoco.gradle") }

description = "AspectJ Support for Java implementations of Grooves"

dependencies {
    api(project(":grooves-api"))

    implementation("com.google.auto.service:auto-service:1.0.1")

    compileOnly("org.jetbrains:annotations:23.0.0")

    testImplementation("com.google.testing.compile:compile-testing:0.19")
    testImplementation("io.reactivex:rxjava:1.3.8")
    testImplementation("io.reactivex:rxjava-reactive-streams:1.2.1")

    testRuntimeOnly("org.jetbrains:annotations:23.0.0")
}
