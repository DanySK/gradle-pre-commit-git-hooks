package org.gradle.kotlin.dsl

import org.danilopianini.gradle.git.hooks.GitHooksExtension
import org.gradle.api.initialization.Settings

inline fun Settings.gitHooks(configure: GitHooksExtension.() -> Unit) {
    // This function is needed because Gradle doesn't generate accessors for settings extensions.
    extensions.getByType<GitHooksExtension>().configure()
}
