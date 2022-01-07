package org.danilopianini.gradle.git.hooks

/**
 * Specialized DSL element for dealing with [conventional commits](https://www.conventionalcommits.org/).
 */
interface ConventionalCommitsContext {

    /**
     * The selected valid commit types.
     */
    val types: Set<String> get() = baseTypes

    /**
     * Adds new supported types.
     */
    fun types(vararg otherTypes: String): Unit = types(otherTypes.toSet())

    /**
     * Adds a new supported types.
     */
    fun types(otherTypes: Set<String>)

    /**
     * configures all the types in [Companion.defaultTypes].
     */
    fun defaultTypes() = types(defaultTypes)

    companion object {

        /**
         * Base types: `fix` and `feat`.
         */
        val baseTypes = setOf("fix", "feat")

        /**
         * Additional types, as listed in the [conventional commits](https://www.conventionalcommits.org/) webpage,
         * plus `refactor`, which is commonly used.
         */
        val defaultTypes: Set<String> = baseTypes + setOf(
            "build",
            "chore",
            "ci",
            "docs",
            "perf",
            "refactor",
            "revert",
            "style",
            "test",
        )
    }
}
