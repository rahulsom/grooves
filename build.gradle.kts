import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    alias(libs.plugins.waena.root)
    alias(libs.plugins.waena.published) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.testLogger) apply false
}

allprojects {
    group = "com.github.rahulsom"
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "checkstyle")
    apply(plugin = "com.adarshr.test-logger")

    configure<CheckstyleExtension> {
        configFile = rootProject.file("gradle/checkstyle/checkstyle.xml")
        toolVersion = "10.7.0"
        maxWarnings = 0
        maxErrors = 0
    }

    configure<TestLoggerExtension> {
        theme = when(System.getProperty("idea.active")) {
            "true" -> ThemeType.PLAIN_PARALLEL
            else -> ThemeType.MOCHA_PARALLEL
        }
        slowThreshold = 5000
    }

    var theProject = project
    var projectPath = theProject.name
    while (theProject.parent != rootProject) {
        projectPath = "${theProject.parent!!.name}-${projectPath}"
        theProject = theProject.parent!!
    }

    sonarqube {
        properties {
            property("sonar.moduleKey", "com.github.rahulsom:grooves:${projectPath}")
        }
    }
}

contacts {
    validateEmails = true
    addPerson("rahulsom@noreply.github.com", delegateClosureOf<nebula.plugin.contacts.Contact> {
        moniker("Rahul Somasunderam")
        roles("owner")
        github("https://github.com/rahulsom")
    })
}

tasks.named("release") { dependsOn("grooves-docs:gitPublishPush") }