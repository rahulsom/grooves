import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    id("org.ajoberstar.git-publish").version("3.0.1")
    id("org.asciidoctor.jvm.convert").version("4.0.2")
}

repositories {
    maven { setUrl("https://repo.spring.io/release") }
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
    contents {
        from(file("$buildDir/asciidoc/html5")) {
            into("manual/${version}")
        }
        if (System.getenv("BRANCH_NAME") == "master") {
            from(file("$buildDir/asciidoc/html5")) {
                into("manual/current")
            }
            from(rootProject.file("grooves-site")) {
                into(".")
            }
        }
    }
    preserve {
        include("**")
    }
}

tasks.named("gitPublishPush") { dependsOn("asciidoctor") }
