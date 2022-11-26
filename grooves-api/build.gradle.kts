plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.rahulsom.waena.published")
}

apply {
    from("$rootDir/gradle/jacoco.gradle")
}

description = "APIs that help in computation of Grooves based Snapshots"

dependencies {
    api(project(":grooves-types"))

    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("org.slf4j:slf4j-api:2.0.5")

    compileOnly("org.codehaus.groovy:groovy:3.0.13")
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("org.projectlombok:lombok:1.18.24")

    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

tasks.withType<Javadoc>().configureEach {
    options {
        (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}