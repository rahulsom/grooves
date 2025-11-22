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

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.groovy)
    implementation(libs.groovy.dateutil)
    implementation(libs.rxjava2)

    implementation(project(":grooves-groovy"))
    implementation(project(":grooves-example-test"))

    runtimeOnly(libs.jakarta.jaxb.api)
    runtimeOnly("com.sun.xml.bind:jaxb-impl:4.0.4") // Jakarta compatible version
    runtimeOnly(libs.activation)
    runtimeOnly(libs.h2)

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.platform.engine)
    testRuntimeOnly(libs.junit.platform.commons)
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