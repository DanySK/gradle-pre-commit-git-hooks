package org.danilopianini.gradle.git.hooks

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create

/**
 * Creates support for the hooks.
 */
open class GradleGitHooksPlugin : Plugin<Any> {
    override fun apply(settings: Any) {
        require(settings is Settings) {
            """
            ${this::class.simpleName} is not meant to be applied manually. It should be applied to settings.gradle.kts:
            plugins {
                id("org.danilopianini.gradle-pre-commit-git-hooks") version "<the version>"
            }
            gitHooks {
                ...
                createHooks()
            }
            """.trimIndent()
        }
        settings.extensions.create<GitHooksExtension>(GitHooksExtension.name, settings)
    }
}
