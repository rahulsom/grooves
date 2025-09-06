plugins {
    alias(libs.plugins.waena.root)
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.buildscan.recipes)
    alias(libs.plugins.mongo) apply false
    alias(libs.plugins.sass) apply false
    alias(libs.plugins.git.publish) apply false
    alias(libs.plugins.asciidoctor.convert) apply false
    alias(libs.plugins.jooq) apply false
    alias(libs.plugins.flyway) apply false
    alias(libs.plugins.liberty) apply false
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
