plugins {
    id("com.gradle.enterprise") version "3.8"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

rootProject.name = "Template-for-Gradle-Plugins"
enableFeaturePreview("VERSION_CATALOGS")
