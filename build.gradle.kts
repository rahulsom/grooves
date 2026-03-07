import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    alias(libs.plugins.waena.root)
    alias(libs.plugins.waena.published) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.spotless)
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.testLogger) apply false
    id("com.github.rahulsom.grooves.count-tests")
}

allprojects {
    group = "com.github.rahulsom"
}

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "checkstyle")
    apply(plugin = "com.adarshr.test-logger")

    configure<CheckstyleExtension> {
        configFile = rootProject.file("gradle/checkstyle/checkstyle.xml")
        toolVersion = rootProject.libs.versions.checkstyle.get()
        maxWarnings = 0
        maxErrors = 0
    }

    configure<TestLoggerExtension> {
        theme = when (System.getProperty("idea.active")) {
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
    with(addPerson("rahulsom@noreply.github.com")) {
        moniker("Rahul Somasunderam")
        roles("owner")
        github("https://github.com/rahulsom")
    }
}


spotless {
    java {
        palantirJavaFormat()
        target("**/*.java")
        targetExclude("**/build/**/*.java")
    }
    kotlin {
        ktlint()
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
    }
    groovy {
        greclipse().configProperties("org.eclipse.jdt.core.formatter.tabulation.char=space")
        target("**/*.groovy")
        targetExclude("**/build/**/*.groovy")
    }
    yaml {
        prettier()
        target("**/*.yml")
        targetExclude(
            "**/build/**/*.yml",
            "**/node_modules/**",
        )
    }
    json {
        prettier()
        target("**/*.json")
        targetExclude(
            "**/.package-lock.json",
            "**/build/**/*.json",
            "**/node_modules/**",
            "**/package-lock.json",
            ".github/renovate.json",
        )
    }
    typescript {
        prettier()
        target("**/*.ts")
        targetExclude("**/build/**/*.ts", "node_modules/**/*.ts")
    }
}

tasks.named("spotlessYaml").configure {
    dependsOn(":grooves-site:npmInstall")
}