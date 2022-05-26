import org.gradle.kotlin.dsl.gitHooks

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks")
}

gitHooks {
    file(".git/hooks").mkdirs()
    commitMsg {
        conventionalCommits()
    }
    createHooks()
}
