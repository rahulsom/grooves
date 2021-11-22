import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("dev.jacomet.logging-capabilities") version "0.+"
    id("io.freefair.aspectj.post-compile-weaving") version "6.3.0"
    id("com.github.rahulsom.waena.published")
    `java-library`
}

loggingCapabilities {
    enforceLogback("testRuntimeClasspath")
}

description = "Simpler implementation of Grooves"

dependencies {
    // configureLombok()
    compileOnly("org.projectlombok:lombok:1.18.22")
    testCompileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    // configureKotlin()
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // configureJUnit()
    testImplementation(platform("org.junit:junit-bom:5.+"))
    testImplementation("org.assertj:assertj-core:3.21.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // configureLogging()
    implementation("org.slf4j:slf4j-api:1.7.32")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.32")
    testRuntimeOnly("org.apache.logging.log4j:log4j-core:2.14.1")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.7")

    implementation("org.aspectj:aspectjrt:1.9.7")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all", "-Xinline-classes")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Configure Slf4j Simple Logger
    systemProperty("org.slf4j.simpleLogger.log.com.github.rahulsom.grooves", "DEBUG")
}