plugins {
    id("com.gradle.enterprise").version("3.12.4")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

rootProject.name = "grooves"

include("grooves-core", "grooves-types", "grooves-api", "grooves-java", "grooves-groovy", "grooves-diagrams", "grooves-example-test")
include("grooves-example-springboot-jpa", "grooves-example-springboot-kotlin", "grooves-example-javaee", "grooves-example-pushstyle")
include("grooves-docs")
