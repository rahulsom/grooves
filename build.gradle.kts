plugins {
    id("com.github.rahulsom.waena.root").version("0.6.1")
    id("org.jetbrains.kotlin.jvm").version("1.8.21").apply(false)
    id("org.jetbrains.kotlin.plugin.allopen").version("1.8.21").apply(false)
    id("org.jlleitschuh.gradle.ktlint").version("11.3.2").apply(false)
    id("org.springframework.boot").version("3.0.4").apply(false)
    id("io.spring.dependency-management").version("1.1.0").apply(false)
    id("org.sonarqube").version("4.2.0.3129")
    id("me.champeau.buildscan-recipes").version("0.2.3")
    id("com.sourcemuse.mongo").version("1.0.7").apply(false)
}

allprojects {
    group = "com.github.rahulsom"
}

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "checkstyle")

    configure<CheckstyleExtension> {
        configFile = rootProject.file("gradle/checkstyle/checkstyle.xml")
        toolVersion = "10.7.0"
        maxWarnings = 0
        maxErrors = 0
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

configure<nebula.plugin.contacts.ContactsExtension> {
    validateEmails = true
    addPerson("rahulsom@noreply.github.com", delegateClosureOf<nebula.plugin.contacts.Contact> {
        moniker("Rahul Somasunderam")
        roles("owner")
        github("https://github.com/rahulsom")
    })
}

tasks.named("release") { dependsOn("grooves-docs:gitPublishPush") }

buildScanRecipes {
    recipes("git-commit", "git-status", "travis-ci", "gc-stats")
}
