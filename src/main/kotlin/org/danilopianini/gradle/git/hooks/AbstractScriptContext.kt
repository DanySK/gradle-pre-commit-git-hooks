package org.danilopianini.gradle.git.hooks

/**
 * Pre-implements [tasks].
 */
abstract class AbstractScriptContext : ScriptContext {
    final override fun tasks(first: Any, vararg others: Any, requireSuccess: Boolean) =
        processTasks(first, *others, requireSuccess = requireSuccess)

    protected abstract fun processTasks(vararg tasks: Any, requireSuccess: Boolean)
}
