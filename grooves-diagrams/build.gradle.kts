import io.miret.etienne.gradle.sass.CompileSass

plugins {
    id("java-library")
    id("groovy")
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.rahulsom.waena.published")
    id("io.miret.etienne.sass").version("1.4.1")
}

apply {
    from("$rootDir/gradle/jacoco.gradle")
}

description = "Asciidoctor Extension to generate Event Sourcing Diagrams like those on https://rahulsom.github.io/grooves"

repositories {
    google()
}

dependencies {
    implementation("com.github.rahulsom:svg-builder:0.4.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("commons-beanutils:commons-beanutils:1.9.4")
    implementation("org.asciidoctor:asciidoctorj:2.5.7")
    implementation("org.slf4j:slf4j-api:2.0.4")

    implementation("javax.xml.bind:jaxb-api:2.3.1")

    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
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