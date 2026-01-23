tasks.register<Exec>("npmInstall") {
    group = "node"
    description = "Installs node modules"
    commandLine("./npmw", "install")
    workingDir = file(".")
    inputs.file("package.json")
    inputs.file("package-lock.json")
    outputs.dir("node_modules")
    dependsOn(rootProject.tasks.named("spotlessTypescript"))
}

tasks.register<Exec>("build") {
    group = "build"
    description = "Builds the site"
    commandLine("./npmw", "run", "build")
    workingDir = file(".")

    inputs.dir("src")
    outputs.dir("dist")

    dependsOn("npmInstall")
}

tasks.register("clean") {
    group = "build"
    description = "Cleans the site distribution"
    delete("dist")
}