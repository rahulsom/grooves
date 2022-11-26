import com.sourcemuse.gradle.plugin.GradleMongoPluginExtension

buildscript {
    repositories {
        mavenCentral()
        maven { setUrl("https://repo.grails.org/grails/core") }
    }
    dependencies {
        classpath("de.flapdoodle.embed:de.flapdoodle.embed.process:2.1.2")
    }
}

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.spring")
    id("groovy")
    id("com.sourcemuse.mongo").version("1.0.7")
}

version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.reactivex:rxjava-reactive-streams")
    implementation("org.springframework:spring-aspects")
    implementation(project(":grooves-api"))
    implementation(project(":grooves-example-test"))

    runtimeOnly("javax.xml.bind:jaxb-api:2.3.1")
    runtimeOnly("com.sun.xml.bind:jaxb-core:4.0.1")
    runtimeOnly("com.sun.xml.bind:jaxb-impl:4.0.1")
    runtimeOnly("javax.activation:activation:1.1.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
    testImplementation("org.spockframework:spock-spring:2.1-groovy-3.0")
    testImplementation("io.projectreactor.addons:reactor-test:3.0.7.RELEASE")
}

tasks.findByName("bootRun")?.dependsOn("startMongoDb")
tasks.findByName("bootRun")?.finalizedBy("stopMongoDb")
tasks.findByName("test")?.dependsOn("startMongoDb")
tasks.findByName("test")?.finalizedBy("stopMongoDb")

configure<GradleMongoPluginExtension> {
    setPort(27021)
}

tasks.withType<Test> {
    useJUnitPlatform()
}