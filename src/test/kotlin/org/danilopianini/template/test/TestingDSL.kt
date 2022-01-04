package org.danilopianini.template.test

import com.uchuhimo.konf.ConfigSpec
import java.io.File

object Root : ConfigSpec("") {
    val tests by required<List<Test>>()
}

data class Test(
    val description: String,
    val configuration: Configuration,
    val expectation: Expectation
)

data class Configuration(val tasks: List<String>, val options: List<String> = emptyList())

data class Expectation(
    val file_exists: List<ExistingFile> = emptyList(),
    val success: List<String> = emptyList(),
    val failure: List<String> = emptyList(),
    val output_contains: List<String> = emptyList()
)

data class ExistingFile(val name: String, val contents: String = ".*", val everyLine: Boolean = false) {
    private fun Sequence<Boolean>.matches(): Boolean = if (everyLine) all { it } else any { it }
    fun isValid() = with(File(name)) {
        exists() && readLines().asSequence().map { it.matches(Regex(contents)) }.matches()
    }
}
