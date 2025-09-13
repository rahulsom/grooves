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

    runtimeOnly(libs.jaxb.api)
    runtimeOnly(libs.sunjaxb.core)
    runtimeOnly(libs.sunjaxb.impl)
    runtimeOnly(libs.activation)
    runtimeOnly(libs.h2)

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly(libs.junit.platform.launcher)
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