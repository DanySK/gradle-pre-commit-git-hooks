package org.danilopianini.template.test

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import io.github.classgraph.ClassGraph
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.slf4j.LoggerFactory
import java.io.File

class Tests : StringSpec(
    {
        val pluginClasspathResource = ClassLoader.getSystemClassLoader()
            .getResource("plugin-classpath.txt")
            ?: throw IllegalStateException("Did not find plugin classpath resource, run \"testClasses\" build task.")
        val classpath = pluginClasspathResource.openStream().bufferedReader().use { reader ->
            reader.readLines().map { File(it) }
        }
        val scan = ClassGraph()
            .enableAllInfo()
            .acceptPackages(Tests::class.java.`package`.name)
            .scan()
        scan.getResourcesWithLeafName("test.yaml")
            .flatMap {
                log.debug("Found test list in $it")
                val yamlFile = File(it.classpathElementFile.absolutePath + "/" + it.path)
                val testConfiguration = Config {
                    addSpec(Root)
                }.from.yaml.inputStream(it.open())
                testConfiguration[Root.tests].map { it to yamlFile.parentFile }
            }
            .forEach { (test, location) ->
                log.debug("Test to be executed: $test from $location")
                val testFolder = folder {
                    location.copyRecursively(this.root)
                }
                log.debug("Test has been copied into $testFolder and is ready to get executed")
                test.description {
                    val result = GradleRunner.create()
                        .withProjectDir(testFolder.root)
                        .withPluginClasspath(classpath)
                        .withArguments(test.configuration.tasks + test.configuration.options)
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
                        with(File("${testFolder.root.absolutePath}/$it")) {
                            shouldExist()
                            shouldBeAFile()
                        }
                    }
                }
            }
    }
) {
    companion object {
        val log = LoggerFactory.getLogger(Tests::class.java)

        private fun BuildResult.outcomeOf(name: String) = task(":$name")
            ?.outcome
            ?: throw IllegalStateException("Task $name was not present among the executed tasks")

        private fun folder(closure: TemporaryFolder.() -> Unit) = TemporaryFolder().apply {
            create()
            closure()
        }
    }
}
