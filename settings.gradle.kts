plugins {
    id("com.gradle.develocity").version("4.3")
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
    }
}

rootProject.name = "grooves"

include("grooves-core", "grooves-types", "grooves-api", "grooves-java", "grooves-groovy", "grooves-diagrams", "grooves-example-test")
include("grooves-example-springboot-jpa", "grooves-example-springboot-kotlin", "grooves-example-javaee", "grooves-example-pushstyle")
include("grooves-docs", "grooves-site")
