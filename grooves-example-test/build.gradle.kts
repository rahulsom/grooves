plugins {
    id("java-library")
    alias(libs.plugins.waena.published)
}


description = "Standard Tests for Grooves"

dependencies {
    api(project(":grooves-api"))
    api(libs.okhttp)
    api(libs.junit.api)
    api(libs.junit.params)
    api(libs.assertj.core)

    implementation(libs.rxjava2)
    implementation(libs.slf4j.api)

    implementation("xerces:xercesImpl:2.12.2")
    implementation(libs.jackson.databind)
    implementation(libs.groovy) // for GroovyEventsDsl support
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation(libs.beanutils)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}