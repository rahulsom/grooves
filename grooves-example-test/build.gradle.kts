plugins {
    id("java-library")
    alias(libs.plugins.waena.published)
}


description = "Standard Tests for Grooves"

dependencies {
    annotationProcessor(libs.lombok)

    api(libs.assertj.core)
    api(libs.jackson.databind)
    api(libs.junit.api)
    api(libs.junit.params)
    api(libs.okhttp)
    api(project(":grooves-api"))

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)

    implementation(libs.beanutils)
    implementation(libs.groovy)
    implementation(libs.httpcomponents.client)

    implementation(libs.rxjava2)
    implementation(libs.slf4j.api)
    implementation(libs.xerces)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}