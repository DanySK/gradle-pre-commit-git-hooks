package org.danilopianini.gradle.git.hooks

import java.net.URL

/**
 * Specialized hook context for commit-msg.
 */
class CommitMsgScriptContext : CommonScriptContext("commit-msg") {

    /**
     * Pre-configures the commit-msg script to check for a valid
     * [conventional commit](https://www.conventionalcommits.org/)
     * message.
     */
    fun conventionalCommits(configuration: ConventionalCommitsContext.() -> Unit = { defaultTypes() }) {
        val types = object : ConventionalCommitsContext {
            override var types = super.types

            override fun types(otherTypes: Set<String>) {
                types = types + otherTypes
            }
        }.apply(configuration).types
        from("") {
            val script: URL = requireNotNull(Thread.currentThread().contextClassLoader.getResource(SCRIPT_PATH)) {
                "Unable to load $SCRIPT_PATH, this is likely a bug in the ${GitHooksExtension.name} plugin"
            }
            script.readText().replace("# INJECT_TYPES", types.joinToString(separator = " "))
        }
    }

    companion object {
        private const val SCRIPT_PATH = "org/danilopianini/gradle/git/hooks/conventional-commit-message.sh"
    }
}
