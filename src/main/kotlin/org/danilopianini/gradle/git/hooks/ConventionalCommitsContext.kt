package org.danilopianini.gradle.git.hooks

interface ConventionalCommitsContext {

    val types: Set<String> get() = baseTypes

    fun types(vararg otherTypes: String): Unit = types(otherTypes.toSet())

    fun types(otherTypes: Set<String>)

    fun defaultTypes() = types(defaultTypes)

    companion object {
        val baseTypes = setOf("fix", "feat")

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