plugins {
    id("com.gradle.enterprise") version "3.10.2"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

rootProject.name = "gradle-pre-commit-git-hooks"
enableFeaturePreview("VERSION_CATALOGS")
