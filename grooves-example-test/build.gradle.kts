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
    api("org.codehaus.groovy.modules.http-builder:http-builder:0.7.1")
    api("org.spockframework:spock-core:2.1-groovy-3.0")

    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("org.slf4j:slf4j-api:2.0.4")

    implementation("xerces:xercesImpl:2.12.2")
    implementation("org.codehaus.groovy:groovy-all:3.0.13")
    implementation("org.codehaus.groovy:groovy-dateutil:3.0.13")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("commons-beanutils:commons-beanutils:1.9.4")

    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("org.projectlombok:lombok:1.18.24")

    annotationProcessor("org.projectlombok:lombok:1.18.24")
}