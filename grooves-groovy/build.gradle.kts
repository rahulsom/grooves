plugins {
    id("java-library")
    id("groovy")
    id("com.github.rahulsom.waena.published")
}

apply {
    from("$rootDir/gradle/jacoco.gradle")
}

description = "Support for completeness of queries in Groovy code"

dependencies {
    api(project(":grooves-api"))

    implementation(libs.groovy)

    testImplementation(libs.rxjava.core)
    testImplementation(libs.rxjava.reactivestreams)
    testImplementation(libs.spock.core)

    testRuntimeOnly(libs.jetbrains.annotations)
}

tasks.withType<Test> {
    useJUnitPlatform()
}