package org.danilopianini.gradle.git.hooks

import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import java.io.File
import java.io.Serializable

/**
 * DSL entry point, to be applied to [settings].gradle.kts.
 */
open class GitHooksExtension(val settings: Settings) : Serializable {
    private var hooks: Map<String, String> = emptyMap()
    private var pathHasBeenManuallySet = false

    /**
     * The git repository root. If unset, it will be searched recursively from the project root towards the
     * filesystem root.
     */
    var repoRoot: File = settings.settingsDir
        set(value) {
            require(value.isGitRoot()) {
                "${value.absolutePath} is not a valid git root (it must contain a .git folder)"
            }
            pathHasBeenManuallySet = true
            field = value
        }
        get() =
            field.takeIf { pathHasBeenManuallySet }
                ?: generateSequence(field) { it.parentFile }.find { it.isGitRoot() }
                ?: error(
                    """
                    No git root could be found in ${field.absolutePath} or any of its parent directories.
                    You may want to set it manually with:
                    gitHooks {
                        repoRoot = File("<path>")
                    }
                    """.trimIndent(),
                )

    private val gitDir get() = File(repoRoot, ".git")

    private inline fun <H : ScriptContext> hook(
        context: H,
        configuration: H.() -> Unit,
    ) {
        require(!hooks.containsKey(context.name)) {
            "it looks like the hook ${context.name} is being defined twice"
        }
        hooks = hooks + (context.name to context.apply(configuration).script)
    }

    /**
     * Defines a new hook with an arbitrary name.
     */
    fun hook(
        hookName: String,
        configuration: ScriptContext.() -> Unit,
    ) = hook(CommonScriptContext(hookName), configuration)

    /**
     * Pre-commit hook.
     */
    fun preCommit(configuration: ScriptContext.() -> Unit) = hook("pre-commit", configuration)

    /**
     * Commit-msg hook.
     */
    fun commitMsg(configuration: CommitMsgScriptContext.() -> Unit): Unit =
        hook(CommitMsgScriptContext(), configuration)

    /**
     * To be called to force the hook creation in case of necessity.
     * If passed `true`, overwrites in case the script is already present and different than expected.
     */
    fun createHooks(overwriteExisting: Boolean = false) {
        if (gitDir.isFile && "gitdir:" in gitDir.readText()) {
            // gets a logger from the settings object
            Logging.getLogger(GitHooksExtension::class.java).warn(
                """
                |The git root does not contain a .git folder, but a .git file with a gitdir: directive.
                |This is typically an indication that the git repository is a detached worktree.
                |Hooks generation in detached worktrees is not supported, see:
                |https://github.com/DanySK/gradle-pre-commit-git-hooks/issues/396
                """.trimMargin().replace(Regex("\\R"), "\n"),
            )
        } else {
            actuallyCreateHooks(overwriteExisting)
        }
    }

    private fun actuallyCreateHooks(overwriteExisting: Boolean) {
        if (gitDir.isFile) {
            error(
                """
                ${gitDir.absolutePath} exists but it is not a directory. It contains:
                ${gitDir.readText()}
                If you are working on a detached worktree, this is likely a regression of issue #396, see:
                https://github.com/DanySK/gradle-pre-commit-git-hooks/issues/396
                """.trimIndent(),
            )
        }
        val hooksFolder = File(gitDir, "hooks")
        if (!hooksFolder.exists()) {
            check(hooksFolder.mkdirs()) {
                "Directory ${hooksFolder.absolutePath} does not exist and can not be created"
            }
        }
        check(hooksFolder.exists()) {
            "${hooksFolder.absolutePath} should have been initialized, but it does not exist"
        }
        check(hooksFolder.isDirectory) {
            "${hooksFolder.absolutePath} has been initialized, but it is a file, not a directory"
        }
        hooks.forEach { (name, script) ->
            val hook = File(hooksFolder, name)
            if (!hook.exists()) {
                require(hook.createNewFile()) { "Cannot create file ${hook.absolutePath}" }
                hook.writeScript(script)
            } else {
                val oldScript = hook.readText()
                if (oldScript != script) {
                    println(
                        """
                        |The hook $name exists, but its content differs from the one generated by the git-hooks plugin.
                        |
                        |Original content:
                        ${oldScript.withMargins()}
                        |
                        |New content:
                        ${script.withMargins()}
                        |
                        |If you want to overwrite this file on every change add true to the createHooks(true) 
                        |method in your settings.gradle.kts
                        """.trimMargin().lines().joinToString(separator = "\n") {
                            "WARNING: $it"
                        },
                    )
                    if (overwriteExisting) {
                        println("WARNING: Overwriting git hook $name")
                        hook.writeScript(script)
                    }
                }
            }
        }
    }

    internal companion object {
        private const val serialVersionUID = 2L

        /**
         * Extension name.
         */
        internal const val NAME: String = "gitHooks"

        private fun String.withMargins() = lines().joinToString(separator = "\n|", prefix = "|")

        private fun File.writeScript(script: String) {
            writeText(script)
            setExecutable(true)
        }

        private fun File.isGitRoot(): Boolean =
            listFiles()
                ?.any { file -> file.name == ".git" }
                ?: false
    }
}
