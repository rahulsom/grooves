plugins {
    id("java-library")
    alias(libs.plugins.waena.published)
}


description = "Standard Tests for Grooves"

dependencies {
    annotationProcessor(libs.lombok)
    api(project(":grooves-api"))
    api(libs.assertj.core)
    api(libs.junit.api)
    api(libs.junit.params)
    api(libs.okhttp)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)
    implementation(libs.beanutils)
    implementation(libs.groovy) // for GroovyEventsDsl support
    implementation(libs.httpcomponents.client)
    implementation(libs.jackson.databind)
    implementation(libs.rxjava2)
    implementation(libs.slf4j.api)
    implementation(libs.xerces)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}