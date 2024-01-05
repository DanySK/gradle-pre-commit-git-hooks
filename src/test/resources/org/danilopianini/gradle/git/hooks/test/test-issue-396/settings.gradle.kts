import org.gradle.kotlin.dsl.gitHooks

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks")
}

gitHooks {
    val target = file("git-config")
    check(target.exists()) {
        "${target.absolutePath} does not exist"
    }
    val destination = file(".git")
    check(target.renameTo(destination)) {
        "could not rename ${target.absolutePath} to ${destination.absolutePath}"
    }
    commitMsg {
        conventionalCommits()
    }
    createHooks()
}
