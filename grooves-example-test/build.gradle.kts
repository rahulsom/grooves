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
    api("org.codehaus.groovy.modules.http-builder:http-builder:0.7.2")
    api(libs.spock.core)

    implementation(libs.rxjava2)
    implementation(libs.slf4j.api)

    implementation("xerces:xercesImpl:2.12.2")
    implementation(libs.groovy)
    implementation(libs.groovy.dateutil)
    implementation(libs.groovy.json)
    implementation(libs.groovy.xml)
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation(libs.beanutils)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)
}