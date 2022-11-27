plugins {
    id("com.gradle.enterprise").version("3.11.4")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

rootProject.name = "grooves"

include("grooves-core")
include("grooves-types")
include("grooves-api")
include("grooves-java")
include("grooves-groovy")
include("grooves-diagrams")
include("grooves-docs")
include("grooves-example-test")
include("grooves-example-springboot-jpa")
include("grooves-example-springboot-kotlin")
include("grooves-example-javaee")
include("grooves-example-pushstyle")
