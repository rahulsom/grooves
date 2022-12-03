import com.sourcemuse.gradle.plugin.GradleMongoPluginExtension

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.spring")
    id("groovy")
    id("com.sourcemuse.mongo")
}

version = "0.0.1-SNAPSHOT"

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

    runtimeOnly(libs.jaxb.api)
    runtimeOnly(libs.sunjaxb.core)
    runtimeOnly(libs.sunjaxb.impl)
    runtimeOnly(libs.activation)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.spock.core)
    testImplementation(libs.spock.spring)
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