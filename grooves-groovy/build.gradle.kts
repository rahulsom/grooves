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

    implementation("org.codehaus.groovy:groovy:3.0.13")

    testImplementation("io.reactivex:rxjava:1.3.8")
    testImplementation("io.reactivex:rxjava-reactive-streams:1.2.1")
    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")

    testRuntimeOnly("org.jetbrains:annotations:23.0.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}