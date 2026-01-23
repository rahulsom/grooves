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
    asciidoctorExt(project(":grooves-diagrams"))
    asciidoctorExt(libs.spring.asciidoctor)
}

project.version = rootProject.version.toString()

tasks.withType<AsciidoctorTask> {
    sourceDir(file("src/docs/asciidoc"))
    // baseDir (file("src/docs/asciidoc"))
    sources {
        include("index.adoc")
    }
    configurations("asciidoctorExt")

    attributes(
        mutableMapOf(
            "sourcedir" to rootDir.absolutePath,
            "includedir" to file("src/docs/asciidoc"),
            "toc" to "left",
            "icons" to "font",
            "stylesheet" to "grooves.css",
            "source-highlighter" to "rouge",
        )
    )

    resources {
        from("src/docs/asciidoc") {
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
        from(file("${layout.buildDirectory.get()}/asciidoc/html5")) {
            into("manual/${version}")
        }
        if (System.getenv("GITHUB_REF_NAME") == "main" || System.getenv("BRANCH_NAME") == "main") {
            from(file("${layout.buildDirectory.get()}/asciidoc/html5")) {
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
    dependsOn("configureGit", ":grooves-site:build")
}

tasks.named("gitPublishPush") { dependsOn("asciidoctor") }

tasks.named("check").configure {
    dependsOn("asciidoctor")
}