plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("dev.jacomet.logging-capabilities") version "0.+"
    id("io.freefair.aspectj.post-compile-weaving") version "6.5.1"
    id("com.github.rahulsom.waena.published")
    id("java-library")
}

loggingCapabilities {
    enforceLogback("testRuntimeClasspath")
}

description = "Simpler implementation of Grooves"

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation(platform("org.junit:junit-bom:5.+"))
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.slf4j:slf4j-api:2.0.5")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.5")
    testRuntimeOnly("org.apache.logging.log4j:log4j-core:2.19.0")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.5")

    implementation("org.aspectj:aspectjrt:1.9.9.1")
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Configure Slf4j Simple Logger
    systemProperty("org.slf4j.simpleLogger.log.com.github.rahulsom.grooves", "DEBUG")
}