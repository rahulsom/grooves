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

tasks.named("release") { dependsOn("grooves-docs:gitPublishPush") }

tasks.register("countTests") {
    description = "Count total number of tests executed across all submodules"
    group = "verification"

    subprojects.forEach { s ->
        dependsOn(s.tasks.withType<Test>())
    }

    doLast {
        var totalTests = 0
        var totalFailures = 0
        var totalErrors = 0
        var totalSkipped = 0
        val moduleResults = mutableMapOf<String, Int>()

        // Count current tests
        subprojects.forEach { subproject ->
            val testResultsDir = subproject.layout.buildDirectory.dir("test-results/test").get().asFile
            if (testResultsDir.exists()) {
                val xmlFiles = testResultsDir.listFiles { file ->
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
                    moduleResults[subproject.name] = moduleTests
                    totalTests += moduleTests
                }
            }
        }

        // Read previous test counts
        val testCountsFile = file("test-counts.properties")
        val previousCounts = mutableMapOf<String, Int>()
        if (testCountsFile.exists()) {
            testCountsFile.readLines().forEach { line ->
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
            val indicator = when {
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
        testCountsFile.writeText(
            """# Test counts per module - automatically updated
# Format: module.name=test.count

"""
        )

        moduleResults.toSortedMap().forEach { (module, count) ->
            testCountsFile.appendText("$module=$count\n")
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

// Auto-run countTests when build or check tasks are executed
tasks.named("check") {
    dependsOn("countTests")
}

// Note: build task depends on check, so it will automatically run countTests via check
// But we can also add it directly to build for clarity
tasks.named("build") {
    dependsOn("countTests")
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
        targetExclude("**/build/**/*.yml")
    }
    json {
        prettier()
        target("**/*.json")
        targetExclude("**/build/**/*.json", "**/package-lock.json", "**/.package-lock.json", ".github/renovate.json")
    }
    typescript {
        prettier()
        target("**/*.ts")
        targetExclude("**/build/**/*.ts", "node_modules/**/*.ts")
    }
}