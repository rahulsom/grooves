import io.miret.etienne.gradle.sass.CompileSass

plugins {
    id("java-library")
    id("groovy")
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.rahulsom.waena.published")
    id("io.miret.etienne.sass").version("1.5.2")
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

    implementation(libs.jaxb.api)

    testImplementation(libs.spock.core)
}

tasks.withType<CompileSass> {
    setSourceDir(project.file("$projectDir/src/main/resources"))
    outputDir = project.file("$buildDir/generated/css")
}

sourceSets {
    main {
        resources.srcDirs("$buildDir/generated/css")
    }
}

tasks.getByName("processResources").dependsOn("compileSass")
tasks.getByName("sourceJar").dependsOn("compileSass")

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}