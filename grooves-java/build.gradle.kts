plugins {
    id("java-library")
    id("com.github.rahulsom.waena.published")
}

apply { from("$rootDir/gradle/jacoco.gradle") }

description = "AspectJ Support for Java implementations of Grooves"

dependencies {
    api(project(":grooves-api"))

    implementation(libs.autoservice)

    compileOnly(libs.jetbrains.annotations)

    testImplementation("com.google.testing.compile:compile-testing:0.19")
    testImplementation(libs.rxjava.core)
    testImplementation(libs.rxjava.reactivestreams)

    testRuntimeOnly(libs.jetbrains.annotations)
}
