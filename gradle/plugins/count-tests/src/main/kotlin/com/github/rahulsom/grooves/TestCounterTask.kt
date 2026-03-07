package com.github.rahulsom.grooves

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class TestCounterTask : DefaultTask() {
    @get:Input
    abstract val testResultDirs: MapProperty<String, File>

    @get:InputFiles
    @get:Optional
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val testResultFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val testCountsFile: RegularFileProperty

    @TaskAction
    fun count() {
        var totalTests = 0
        var totalFailures = 0
        var totalErrors = 0
        var totalSkipped = 0
        val moduleResults = mutableMapOf<String, Int>()

        testResultDirs.get().forEach { (moduleName, testResultsDir) ->
            if (testResultsDir.exists()) {
                val xmlFiles =
                    testResultsDir.listFiles { file ->
                        file.name.startsWith("TEST-") && file.name.endsWith(".xml")
                    }

                var moduleTests = 0
                xmlFiles?.forEach { xmlFile ->
                    val content = xmlFile.readText()
                    val testsMatch = Regex("""tests="(\d+)"""").find(content)
                    val failuresMatch = Regex("""failures="(\d+)"""").find(content)
                    val errorsMatch = Regex("""errors="(\d+)"""").find(content)
                    val skippedMatch = Regex("""skipped="(\d+)"""").find(content)

                    testsMatch?.let { moduleTests += it.groupValues[1].toInt() }
                    failuresMatch?.let { totalFailures += it.groupValues[1].toInt() }
                    errorsMatch?.let { totalErrors += it.groupValues[1].toInt() }
                    skippedMatch?.let { totalSkipped += it.groupValues[1].toInt() }
                }

                if (moduleTests > 0) {
                    moduleResults[moduleName] = moduleTests
                    totalTests += moduleTests
                }
            }
        }

        // Read previous test counts
        val testCountsFileObj = testCountsFile.get().asFile
        val previousCounts = mutableMapOf<String, Int>()
        if (testCountsFileObj.exists()) {
            testCountsFileObj.readLines().forEach { line ->
                if (line.contains("=") && !line.startsWith("#")) {
                    val (module, count) = line.split("=", limit = 2)
                    previousCounts[module.trim()] = count.trim().toInt()
                }
            }
        }

        // Check for test count decreases
        val decreases = mutableMapOf<String, Pair<Int, Int>>()
        val increases = mutableMapOf<String, Pair<Int, Int>>()

        moduleResults.forEach { (module, currentCount) ->
            val previousCount = previousCounts[module] ?: 0
            when {
                currentCount < previousCount -> decreases[module] = Pair(previousCount, currentCount)
                currentCount > previousCount -> increases[module] = Pair(previousCount, currentCount)
            }
        }

        // Display results
        println("\n=== Test Count Summary ===")
        moduleResults.toSortedMap().forEach { (module, count) ->
            val previous = previousCounts[module] ?: 0
            val indicator =
                when {
                    count > previous -> " ↗ (+${count - previous})"
                    count < previous -> " ↘ (-${previous - count})"
                    else -> ""
                }
            println("  $module: $count tests$indicator")
        }
        println("  " + "─".repeat(40))
        println("  Total: $totalTests tests")

        if (totalFailures > 0 || totalErrors > 0 || totalSkipped > 0) {
            println("\n=== Additional Statistics ===")
            if (totalFailures > 0) println("  Failures: $totalFailures")
            if (totalErrors > 0) println("  Errors: $totalErrors")
            if (totalSkipped > 0) println("  Skipped: $totalSkipped")
        }

        // Fail build if test counts decreased
        if (decreases.isNotEmpty()) {
            println("\n❌ Build failed: Test count decreased in the following modules:")
            decreases.forEach { (module, counts) ->
                println("  $module: ${counts.first} → ${counts.second} (-${counts.first - counts.second})")
            }
            println("\nTest counts should never decrease. Please investigate and add missing tests.")
            throw GradleException("Test count verification failed: ${decreases.size} module(s) have fewer tests")
        }

        // Update test counts file
        testCountsFileObj.writeText(
            """# Test counts per module - automatically updated
# Format: module.name=test.count

""",
        )

        moduleResults.toSortedMap().forEach { (module, count) ->
            testCountsFileObj.appendText("$module=$count\n")
        }

        if (increases.isNotEmpty()) {
            println("\n✅ Test counts increased in ${increases.size} module(s) - file updated:")
            increases.forEach { (module, counts) ->
                println("  $module: ${counts.first} → ${counts.second} (+${counts.second - counts.first})")
            }
        }

        println()
    }
}