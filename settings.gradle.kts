plugins {
    id("com.gradle.enterprise") version "3.13.3"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.7"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

gitHooks {
    preCommit {
        tasks("ktlintCheck")
    }
    commitMsg { conventionalCommits() }
    createHooks()
}

rootProject.name = "gradle-pre-commit-git-hooks"
