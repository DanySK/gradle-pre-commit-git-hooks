package org.danilopianini.gradle.git.hooks

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

/**
 * Implements a Script DSL valid for any hook.
 */
open class CommonScriptContext(override val name: String) : AbstractScriptContext() {
    final override var script: String = ""
        private set

    override fun appendScript(script: () -> String) {
        require(this.script.isNotEmpty()) {
            """
                An append was requested to an uninitialized script $name. Configure as follows, instead:
                gitHooks {
                    <your script block> {
                        from {
                            <heading, if any>
                        }
                        <remainder of your configuration>
                    }
                }
            """.trimIndent()
        }
        this.script += script() + '\n'
    }

    final override fun from(shebang: String?, script: () -> String) {
        require(this.script.isEmpty()) {
            """
            The $name git hook is being defined twice, and this is likely an error. Formerly:
            
            ${this.script}
            
            then:
            
            ${
            runCatching { script() }
                .getOrElse { "Something whose evaluation this error:\n${it.stackTraceToString()}" }
            }
            """.trimIndent()
        }
        this.script = (shebang?.takeIf { it.isNotBlank() }?.let { "$it\n" } ?: "") + script()
    }

    final override fun processTasks(vararg tasks: Any, requireSuccess: Boolean) {
        val names = tasks.map { task ->
            when (task) {
                is String -> task
                is Task -> task.name
                is TaskProvider<*> -> task.name
                else -> throw IllegalStateException(
                    "Object '$task' with type '${task::class.simpleName}' cannot produce valid task names"
                )
            }
        }
        if (script.isEmpty()) {
            from { "" }
        }
        appendScript {
            """
            ${ if (requireSuccess) "set -e" else "" }
            ./gradlew ${names.joinToString(separator = " ")}
            ${ if (requireSuccess) "set +e" else "" }
            """.trimIndent()
        }
    }
}
