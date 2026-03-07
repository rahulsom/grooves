plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("countTests") {
            id = "com.github.rahulsom.grooves.count-tests"
            implementationClass = "com.github.rahulsom.grooves.CountTestsPlugin"
        }
    }
}
