package org.danilopianini.gradle.git.hooks

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.net.URL

/**
 * Collects the DSL elements for defining scripts.
 */
interface ScriptContext {

    /**
     * Hook name.
     */
    val name: String

    /**
     * Script content. To be fetched only when the configuration is complete.
     */
    val script: String

    /**
     * Appends the result of the provided function to the existing script.
     */
    fun appendScript(script: () -> String)

    /**
     * Generates a script from the provided [file].
     */
    fun from(file: File) = from("") { file.readText() }

    /**
     * Generates a script from the provided [url].
     */
    fun from(url: URL) = from("") { url.readText() }

    /**
     * Generates a script from the provided [url].
     */
    fun from(url: String) = from(URL(url))

    /**
     * Generates a script with either the provided [shebang] or with a shebang invoking bash,
     * and with the result of the provided function.
     */
    fun from(shebang: String? = "#!/usr/bin/env bash", script: () -> String)

    /**
     * Adds the provided tasks to the script, by invoking `./gradlew <taskname>`.
     * By default, a failure of the task implies a failure of the commit.
     * To run a task without considering the failure critical, pass `[requireSuccess] = false`.
     */
    fun tasks(name: String, vararg otherNames: String, requireSuccess: Boolean = true) =
        tasks(name as Any, *otherNames, requireSuccess = requireSuccess)

    /**
     * Adds the provided tasks to the script, by invoking `./gradlew <taskname>`.
     * By default, a failure of the task implies a failure of the commit.
     * To run a task without considering the failure critical, pass `[requireSuccess] = false`.
     */
    fun tasks(task: Task, vararg otherTasks: Task, requireSuccess: Boolean = true) =
        tasks(task as Any, *otherTasks, requireSuccess = requireSuccess)

    /**
     * Adds the provided tasks to the script, by invoking `./gradlew <taskname>`.
     * By default, a failure of the task implies a failure of the commit.
     * To run a task without considering the failure critical, pass `[requireSuccess] = false`.
     */
    fun tasks(task: TaskProvider<*>, vararg otherTasks: TaskProvider<*>, requireSuccess: Boolean = true) =
        tasks(task as Any, *otherTasks, requireSuccess = requireSuccess)

    /**
     * Adds the provided tasks to the script, by invoking `./gradlew <taskname>`.
     * By default, a failure of the task implies a failure of the commit.
     * To run a task without considering the failure critical, pass `[requireSuccess] = false`.
     */
    fun tasks(first: Any, vararg others: Any, requireSuccess: Boolean = true)
}
