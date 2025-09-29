tasks.register<Exec>("npmInstall") {
    commandLine("./npmw", "install")
    workingDir = file(".")
    inputs.file("package.json")
    inputs.file("package-lock.json")
    outputs.dir("node_modules")
}

tasks.register<Exec>("build") {
    commandLine("./npmw", "run", "build")
    workingDir = file(".")

    inputs.dir("src")
    inputs.dir("public")
    outputs.dir("dist")

    dependsOn("npmInstall")
}

tasks.register("clean") {
    delete("dist")
}