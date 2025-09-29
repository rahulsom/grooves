tasks.register("build") {
    sync {
        from(".")
        into("dist")
        exclude("build.gradle.kts")
    }
}

tasks.register("clean") {
    delete("dist")
}