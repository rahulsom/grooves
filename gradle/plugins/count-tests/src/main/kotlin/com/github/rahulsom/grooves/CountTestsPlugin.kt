package com.github.rahulsom.grooves

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class CountTestsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project != project.rootProject) {
            return
        }

        project.tasks.register("countTests", TestCounterTask::class.java) {
            description = "Count total number of tests executed across all submodules"
            group = "verification"

            testCountsFile.set(project.layout.projectDirectory.file("test-counts.properties"))

            project.subprojects.forEach { s ->
                val dir = s.layout.buildDirectory.dir("test-results/test")
                testResultDirs.put(s.name, dir.map { it.asFile })
                testResultFiles.from(dir)
                dependsOn(s.tasks.withType(Test::class.java))
            }
        }

        // Auto-run countTests when build or check tasks are executed
        project.tasks.named("check") {
            dependsOn("countTests")
        }

        project.tasks.named("build") {
            dependsOn("countTests")
        }
    }
}