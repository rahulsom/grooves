plugins {
    id("java-library")
    id("groovy")
    id("com.github.rahulsom.waena.published")
}

apply {
    from("$rootDir/gradle/codenarc/codenarc.gradle")
}

description = "Standard Tests for Grooves"

dependencies {
    api(project(":grooves-api"))
    api(libs.okhttp)
    api(libs.spock.core)

    implementation(libs.rxjava2)
    implementation(libs.slf4j.api)

    implementation(libs.xerces)
    implementation(libs.groovy)
    implementation(libs.groovy.dateutil)
    implementation(libs.groovy.json)
    implementation(libs.groovy.xml)
    implementation(libs.httpclient)
    implementation(libs.beanutils)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)
}