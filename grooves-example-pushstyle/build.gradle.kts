import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.kotlin.*

buildscript {
    dependencies {
        classpath(libs.activation)
        classpath(libs.h2)
        classpath(libs.jakarta.jaxb.api)
        classpath(libs.sunjaxb.impl) // Jakarta compatible version
    }
}

plugins {
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
    id("org.jetbrains.kotlin.jvm")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    compileOnly(libs.jakarta.annotation.api)
    implementation(project(":grooves-api"))
    implementation(libs.activation)
    implementation(libs.google.guava)
    implementation(libs.google.guice)
    implementation(libs.groovy.all)
    implementation(libs.h2)
    implementation(libs.jakarta.jaxb.api)
    implementation(libs.jooq.core)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.rxjava2)
    implementation(libs.slf4j.api)
    implementation(libs.sunjaxb.impl) // Jakarta compatible version
    jooqGenerator(libs.h2)
    runtimeOnly(libs.logback.classic)
    testImplementation(libs.awaitility)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

jooq {
    edition.set(JooqEdition.OSS)
    configurations {
        create("main") {
            jooqConfiguration {
                jdbc {
                    driver = "org.h2.Driver"
                    url = "jdbc:h2:file:${project.layout.buildDirectory.dir("schema").get().asFile}"
                    user = "sa"
                    password = ""
                }
                generator {
                    name = "org.jooq.codegen.DefaultGenerator"
                    strategy {
                        name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    }
                    database {
                        name = "org.jooq.meta.h2.H2Database"
                        inputSchema = "public"
                    }
                    generate {
                        isRelations = true
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target {
                        packageName = "grooves.example.pushstyle"
                    }
                }
            }
        }
    }
}

flyway {
    url = "jdbc:h2:file:${layout.buildDirectory.dir("schema").get().asFile}"
    user = "sa"
    password = ""
    schemas = arrayOf("public")
}

tasks.withType<Javadoc> { exclude("example/pushstyle/tables/**") }

tasks.named("generateJooq") { dependsOn("flywayMigrate") }
tasks.named("compileKotlin") { dependsOn("generateJooq") }

tasks.named<Checkstyle>("checkstyleMain") { source = fileTree("src/main/java") }

tasks.withType<Test> {
    useJUnitPlatform()
}