import io.miret.etienne.gradle.sass.CompileSass

plugins {
    id("java-library")
    id("groovy")
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    alias(libs.plugins.waena.published)
    alias(libs.plugins.sass)
}

apply {
    from("$rootDir/gradle/jacoco.gradle")
}

description = "Asciidoctor Extension to generate Event Sourcing Diagrams like those on https://rahulsom.github.io/grooves"

dependencies {
    implementation(libs.svgbuilder)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(libs.beanutils)
    implementation(libs.asciidoctorj)
    implementation(libs.slf4j.api)

    implementation(libs.jakarta.jaxb.api)

    testImplementation(libs.spock.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<CompileSass> {
    setSourceDir(project.file("$projectDir/src/main/resources"))
    outputDir = project.file("${layout.buildDirectory.get()}/generated/css")
}

sourceSets {
    main {
        resources.srcDirs("${layout.buildDirectory.get()}/generated/css")
    }
}

tasks.getByName("processResources").dependsOn("compileSass")
tasks.findByName("sourcesJar")?.dependsOn("compileSass")

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}