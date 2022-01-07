package org.danilopianini.gradle.git.hooks

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.net.URL

interface ScriptContext {
    val name: String
    val script: String
    fun appendScript(script: () -> String)
    fun from(url: URL) = from("") { url.readText() }
    fun from(url: String) = from(URL(url))
    fun from(shebang: String? = "#!/usr/bin/env bash", script: () -> String)
    fun tasks(name: String, vararg otherNames: String, requireSuccess: Boolean = true) =
        tasks(name as Any, *otherNames, requireSuccess = requireSuccess)

    fun tasks(task: Task, vararg otherTasks: Task, requireSuccess: Boolean = true) =
        tasks(task as Any, *otherTasks, requireSuccess = requireSuccess)

    fun tasks(task: TaskProvider<*>, vararg otherTasks: TaskProvider<*>, requireSuccess: Boolean = true) =
        tasks(task as Any, *otherTasks, requireSuccess = requireSuccess)

    fun tasks(first: Any, vararg others: Any, requireSuccess: Boolean = true)
}