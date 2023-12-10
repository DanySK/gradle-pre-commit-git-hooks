import com.lordcodes.turtle.shellRun
import org.gradle.kotlin.dsl.gitHooks

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.lordcodes.turtle:turtle:0.8.0")
    }
}

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks")
}

shellRun(rootProject.projectDir) {
    git.gitInit()
}

gitHooks {
    commitMsg {
        from("#!/bin/sh") { "true" }
    }
    createHooks()
    shellRun(rootProject.projectDir) {
        command("git", listOf("add", "settings.gradle.kts"))
        command("git", listOf("config", "user.name", "test user"))
        command("git", listOf("config", "user.email", "no-reply@github.com"))
        git.commit("this commit should be possible")
    }
}
