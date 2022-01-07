package org.gradle.kotlin.dsl

import org.danilopianini.gradle.git.hooks.GitHooksExtension
import org.gradle.api.initialization.Settings

/**
 * DSL entry point for the git hooks commits.
 * This function is needed because Gradle doesn't generate accessors for settings extensions.
 */
inline fun Settings.gitHooks(configure: GitHooksExtension.() -> Unit) {
    extensions.getByType<GitHooksExtension>().configure()
}
