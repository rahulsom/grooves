plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("groovy")
}

apply {
    from("$rootDir/gradle/codenarc/codenarc.gradle")
    from("$rootDir/gradle/jacoco.gradle")
}

dependencies {
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.groovy)
    implementation(libs.groovy.dateutil)
    implementation(libs.rxjava2)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.web)
    implementation(project(":grooves-example-test"))
    implementation(project(":grooves-groovy"))

    runtimeOnly(libs.activation)
    runtimeOnly(libs.h2)
    runtimeOnly(libs.jakarta.jaxb.api)
    runtimeOnly(libs.sunjaxb.impl)

    testImplementation(libs.spring.boot.starter.test)

    testRuntimeOnly(libs.junit.platform.commons)
    testRuntimeOnly(libs.junit.platform.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

val junit = libs.versions.junit
configurations.testRuntimeClasspath {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.junit.platform") {
                useVersion(junit.platform.get())
            }
            if (requested.group == "org.junit.jupiter") {
                useVersion(junit.core.get())
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("bootJar", Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}