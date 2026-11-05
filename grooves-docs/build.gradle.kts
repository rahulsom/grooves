import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    alias(libs.plugins.git.publish)
    alias(libs.plugins.asciidoctor.convert)
}

repositories {
    maven { setUrl("https://repo.spring.io/release") }
}

asciidoctorj {
    setVersion(libs.versions.asciidoctorj.get())
}

val asciidoctorExt by configurations.creating

dependencies {
    asciidoctorExt(libs.spring.asciidoctor)
    asciidoctorExt(project(":grooves-diagrams"))
}

project.version = rootProject.version.toString()

// Render historical sources with the maintained documentation toolchain.
val manualRoot = file(providers.gradleProperty("manualRoot").getOrElse(rootDir.absolutePath))
val manualSource = file(providers.gradleProperty("manualSource").getOrElse("src/docs/asciidoc"))
val manualVersion = providers.gradleProperty("manualVersion").getOrElse(project.version.toString())

tasks.withType<AsciidoctorTask> {
    notCompatibleWithConfigurationCache("AsciidoctorTask is not compatible with the configuration cache in the current version of the plugin.")
    sourceDir(manualSource)
    baseDirFollowsSourceDir()
    providers.gradleProperty("manualOutput").orNull?.let { setOutputDir(file(it)) }
    sources {
        include("index.adoc")
    }
    configurations("asciidoctorExt")

    attributes(
        mutableMapOf(
            "sourcedir" to manualRoot.absolutePath,
            "includedir" to manualSource,
            "gradle-project-version" to manualVersion,
            "revnumber" to manualVersion,
            "toc" to "left",
            "icons" to "font",
            "stylesheet" to "grooves.css",
            "source-highlighter" to "rouge",
        )
    )

    resources {
        from(manualSource) {
            include("*.css")
        }
    }
}

tasks.named("asciidoctor") { dependsOn(":grooves-diagrams:jar") }

gitPublish {
    repoUri.set("https://github.com/rahulsom/grooves.git")
    branch.set("gh-pages")
    username = System.getenv("GITHUB_ACTOR")
    password = System.getenv("GITHUB_TOKEN")
    contents {
        from(tasks.named("asciidoctor")) {
            into("manual/${version}")
        }
        if (System.getenv("GITHUB_REF_NAME") == "main" || System.getenv("BRANCH_NAME") == "main") {
            from(tasks.named("asciidoctor")) {
                into("manual/current")
            }
            from(rootProject.file("grooves-site/dist")) {
                into(".")
            }
        }
    }
    preserve {
        include("**")
    }
}

tasks.register("configureGit") {
    group = "publishing"
    description = "Configures Git user for GitHub Actions"
    doFirst {
        // Set git user.name and user.email in GitHub Actions environment
        if (System.getenv("GITHUB_ACTIONS") == "true") {
            val execResult1 = ProcessBuilder("git", "config", "--global", "user.name", "GitHub Actions")
                .start()
                .waitFor()
            val execResult2 = ProcessBuilder("git", "config", "--global", "user.email", "actions@github.com")
                .start()
                .waitFor()

            if (execResult1 != 0 || execResult2 != 0) {
                println("Warning: Failed to set git config for GitHub Actions")
            }
        }
    }
}

tasks.named("gitPublishCopy") {
    dependsOn("configureGit", "asciidoctor", ":grooves-site:build")
}

tasks.named("gitPublishPush") { dependsOn("asciidoctor") }

tasks.named("check").configure {
    dependsOn("asciidoctor")
}
