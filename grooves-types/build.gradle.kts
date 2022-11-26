plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.rahulsom.waena.published")
}

apply {
    from("$rootDir/gradle/jacoco.gradle")
}

description = "Types that are used by Grooves to build an Event Sourcing system"

dependencies {
    api("org.reactivestreams:reactive-streams:1.0.4")
    implementation("org.slf4j:slf4j-api:2.0.4")

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}