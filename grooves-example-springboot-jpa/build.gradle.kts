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
    compileOnly("org.jetbrains:annotations:23.0.0")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.codehaus.groovy:groovy")
    implementation("org.codehaus.groovy:groovy-dateutil")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")

    implementation(project(":grooves-groovy"))
    implementation(project(":grooves-example-test"))

    runtimeOnly("javax.xml.bind:jaxb-api:2.3.1")
    runtimeOnly("com.sun.xml.bind:jaxb-core:4.0.1")
    runtimeOnly("com.sun.xml.bind:jaxb-impl:4.0.1")
    runtimeOnly("javax.activation:activation:1.1.1")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("org.spockframework:spock-core:2.1-groovy-3.0")
    testImplementation("org.spockframework:spock-spring:2.3-groovy-4.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}