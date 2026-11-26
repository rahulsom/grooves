import org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTask
import org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjGenericExtension
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain

plugins {
    base
    alias(libs.plugins.git.publish)
    alias(libs.plugins.asciidoctor.jvm)
}

repositories {
    maven { setUrl("https://repo.spring.io/release") }
}

project.version = rootProject.version.toString()

// Render historical sources with the maintained documentation toolchain.
val manualRoot = file(providers.gradleProperty("manualRoot").getOrElse(rootDir.absolutePath))
val manualSource = file(providers.gradleProperty("manualSource").getOrElse("src/docs/asciidoc"))
val manualVersion = providers.gradleProperty("manualVersion").getOrElse(project.version.toString())
val springAsciidoctor = libs.spring.asciidoctor.get()
val groovesDiagrams = dependencies.project(path = ":grooves-diagrams")

asciidoc.toolchains.named<AsciidoctorjToolchain>("asciidoctorj") {
    useAsciidoctorj(libs.versions.asciidoctorj.get())
    asciidocExtensions.register<AsciidoctorjGenericExtension>("manualExtensions") {
        useModule(springAsciidoctor.module.toString(), springAsciidoctor.versionConstraint.requiredVersion)
        useModule(groovesDiagrams)
    }
}

val manual = asciidoc.publications.named("main") {
    sourceSet {
        setSourceDir(manualSource)
        sources("index.adoc")
        resources { include("*.css") }
        attributes {
            addAll(
                mapOf(
                    "sourcedir" to manualRoot.absolutePath,
                    "includedir" to manualSource.absolutePath,
                    "gradle-project-version" to manualVersion,
                    "revnumber" to manualVersion,
                    "toc" to "left",
                    "icons" to "font",
                    "stylesheet" to "grooves.css",
                    "source-highlighter" to "rouge",
                )
            )
        }
    }
    output("asciidoctorj", "html")
}

val renderManual = tasks.named<AsciidoctorTask>(manual.get().taskNameFor("html")) {
    val generated = outputDir
    // The publication directory is reused when rendering different versions.
    doFirst {
        val directory = generated.get().asFile
        check(directory.deleteRecursively()) { "Could not clear generated manual directory $directory" }
    }
}
val renderedManual = renderManual.flatMap { it.outputDir }

val exportManual = providers.gradleProperty("manualOutput").orNull?.let { output ->
    tasks.register<Sync>("exportManual") {
        dependsOn(renderManual)
        from(renderedManual)
        into(file(output))
    }
}

tasks.register("asciidoctor") {
    group = "documentation"
    description = "Renders the HTML manual."
    dependsOn(exportManual ?: renderManual)
}

gitPublish {
    repoUri.set("https://github.com/rahulsom/grooves.git")
    branch.set("gh-pages")
    username = System.getenv("GITHUB_ACTOR")
    password = System.getenv("GITHUB_TOKEN")
    contents {
        from(renderedManual) {
            into("manual/${version}")
        }
        if (System.getenv("GITHUB_REF_NAME") == "main" || System.getenv("BRANCH_NAME") == "main") {
            from(renderedManual) {
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
    dependsOn("configureGit", renderManual, ":grooves-site:build")
}

tasks.named("gitPublishPush") { dependsOn(renderManual) }

tasks.named("check").configure {
    dependsOn(renderManual)
}
