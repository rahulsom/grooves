plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.waena.published)
}

apply {
    from("$rootDir/gradle/jacoco.gradle")
}

description = "APIs that help in computation of Grooves based Snapshots"

dependencies {
    annotationProcessor(libs.lombok)

    api(project(":grooves-types"))

    compileOnly(libs.groovy)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)

    implementation(libs.rxjava2)
    implementation(libs.slf4j.api)
}

tasks.withType<Javadoc>().configureEach {
    options {
        (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}