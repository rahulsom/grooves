tasks.register<Exec>("npmInstall") {
    commandLine("./npmw", "install")
    workingDir = file(".")
    inputs.file("package.json")
    inputs.file("package-lock.json")
    outputs.dir("node_modules")
    dependsOn(rootProject.tasks.named("spotlessTypescript"))
}

tasks.register<Exec>("build") {
    commandLine("./npmw", "run", "build")
    workingDir = file(".")

    inputs.dir("src")
    outputs.dir("dist")

    dependsOn("npmInstall")
}

tasks.register("clean") {
    delete("dist")
}