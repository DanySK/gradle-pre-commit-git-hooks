import org.gradle.kotlin.dsl.gitHooks

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks")
}

gitHooks {
    file(".git").mkdirs()
    commitMsg {
        conventionalCommits()
    }
    createHooks()
}
