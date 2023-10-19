package org.danilopianini.gradle.git.hooks.test

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import io.github.classgraph.ClassGraph
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.internal.hash.Hashing
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class Tests : StringSpec(
    {
        val scan = ClassGraph()
            .enableAllInfo()
            .acceptPackages(Tests::class.java.`package`.name)
            .scan()
        scan.getResourcesWithLeafName("test.yaml")
            .flatMap { resource ->
                log.debug("Found test list in {}", resource)
                val yamlFile = File(resource.classpathElementFile.absolutePath + "/" + resource.path)
                val testConfiguration = Config {
                    addSpec(Root)
                }.from.yaml.inputStream(resource.open())
                testConfiguration[Root.tests].map { it to yamlFile.parentFile }
            }
            .forEach { (test, location) ->
                log.debug("Test to be executed: {} from {}", test, location)
                val testFolder = folder {
                    location.copyRecursively(this.root)
                }
                log.debug("Test has been copied into {} and is ready to get executed", testFolder)
                test.description {
                    val result = GradleRunner.create()
                        .withProjectDir(testFolder.root)
                        .withArguments(test.configuration.tasks + test.configuration.options)
                        .withPluginClasspath()
                        .run { if (test.expectation.failure.isEmpty()) build() else buildAndFail() }
                    println(result.tasks)
                    println(result.output)
                    test.expectation.output_contains.forEach {
                        result.output shouldContain it
                    }
                    test.expectation.success.forEach {
                        result.outcomeOf(it) shouldBe TaskOutcome.SUCCESS
                    }
                    test.expectation.failure.forEach {
                        result.outcomeOf(it) shouldBe TaskOutcome.FAILED
                    }
                    test.expectation.file_exists.forEach {
                        val file = File("${testFolder.root.absolutePath}/${it.name}").apply {
                            shouldExist()
                            shouldBeAFile()
                        }
                        it.validate(file)
                    }
                    test.expectation.post_run_script.takeIf { it.isNotEmpty() }?.also { postRuns ->
                        findShell()?.also { shell ->
                            postRuns.forEach { postRun ->
                                val hash = Hashing.sha512().hashString(postRun)
                                val fileName = "post-run-$hash.sh"
                                with(File(testFolder.root, fileName)) {
                                    writeText(postRun)
                                    setExecutable(true)
                                }
                                val process = ProcessBuilder()
                                    .directory(testFolder.root)
                                    .command(shell, fileName)
                                    .start()
                                val finished = process.waitFor(10, TimeUnit.SECONDS)
                                finished shouldBe true
                                log.debug(process.inputStream.bufferedReader().use { it.readText() })
                                process.exitValue() shouldBe 0
                            }
                        } ?: log.warn(
                            "No known Unix shell available on this system! Tests with scripts won't be executed",
                        )
                    }
                }
            }
    },
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(Tests::class.java)
        private val shells = listOf("sh", "bash", "zsh", "fish", "csh", "ksh")

        private fun BuildResult.outcomeOf(name: String) = requireNotNull(task(":$name")) {
            "Task $name was not present among the executed tasks"
        }.outcome

        private fun folder(closure: TemporaryFolder.() -> Unit) = TemporaryFolder().apply {
            create()
            closure()
        }

        private fun findShell(): String? {
            val paths = System.getenv("PATH").split(Regex(Pattern.quote(File.pathSeparator)))
            return shells.find { shell ->
                paths.any { path ->
                    val executable = File(File(path), shell)
                    executable.exists() && executable.canExecute()
                }
            }
        }
    }
}
