import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.kotlin.*

buildscript {
    dependencies {
        classpath(libs.h2)

        classpath(libs.jakarta.jaxb.api)
        classpath("com.sun.xml.bind:jaxb-impl:4.0.6") // Jakarta compatible version
        classpath(libs.activation)
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway)
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.groovy.all)

    implementation(libs.google.guice)
    implementation(libs.google.guava)

    implementation(libs.h2)
    implementation("org.jooq:jooq")

    implementation(libs.jakarta.jaxb.api)
    implementation("com.sun.xml.bind:jaxb-impl:4.0.6") // Jakarta compatible version
    implementation(libs.activation)
    compileOnly(libs.jakarta.annotation.api)

    implementation(project(":grooves-api"))

    implementation(libs.rxjava2)
    implementation(libs.slf4j.api)

    jooqGenerator(libs.h2)

    runtimeOnly(libs.logback.classic)

    testImplementation(libs.junit.api)
    testImplementation(libs.awaitility)
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