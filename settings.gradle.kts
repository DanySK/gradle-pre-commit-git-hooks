plugins {
    id("com.gradle.enterprise") version "3.10.3"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.0.15"
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
