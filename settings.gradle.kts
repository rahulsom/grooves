plugins {
    id("com.gradle.enterprise").version("3.14.1")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
    }
}

rootProject.name = "grooves"

include("grooves-core", "grooves-types", "grooves-api", "grooves-java", "grooves-groovy", "grooves-diagrams", "grooves-example-test")
include("grooves-example-springboot-jpa", "grooves-example-springboot-kotlin", "grooves-example-javaee", "grooves-example-pushstyle")
include("grooves-docs")
