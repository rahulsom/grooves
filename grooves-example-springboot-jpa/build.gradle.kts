plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("groovy")
}

apply {
    from("$rootDir/gradle/codenarc/codenarc.gradle")
    from("$rootDir/gradle/jacoco.gradle")
}

dependencies {
    compileOnly(libs.jetbrains.annotations)

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.codehaus.groovy:groovy")
    implementation("org.codehaus.groovy:groovy-dateutil")
    implementation(libs.rxjava2)

    implementation(project(":grooves-groovy"))
    implementation(project(":grooves-example-test"))

    runtimeOnly(libs.jaxb.api)
    runtimeOnly(libs.sunjaxb.core)
    runtimeOnly(libs.sunjaxb.impl)
    runtimeOnly("javax.activation:activation:1.1.1")
    runtimeOnly(libs.h2)

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation(libs.spock.core)
    testImplementation(libs.spock.spring)
}

tasks.withType<Test> {
    useJUnitPlatform()
}