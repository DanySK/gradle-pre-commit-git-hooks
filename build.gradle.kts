@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-gradle-plugin`
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.taskTree)
}

/*
 * Project information
 */
group = "org.danilopianini"
description = "A Gradle plugin enforcing pre-commit and commit-msg Git hooks configuration. Conventional-commits-ready."

inner class ProjectInfo {
    val longName = "Gradle pre-commit Git Hooks"
    val website = "https://github.com/DanySK/$name"
    val vcsUrl = "$website.git"
    val scm = "scm:git:$website.git"
    val pluginImplementationClass = "$group.gradle.git.hooks.GradleGitHooksPlugin"
    val tags = listOf("git", "hook", "git hooks", "conventional commits")
}
val info = ProjectInfo()

gitSemVer {
    buildMetadataSeparator.set("-")
}

repositories {
    mavenCentral()
}

multiJvm {
    maximumSupportedJvmVersion.set(latestJavaSupportedByGradle)
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    api(kotlin("stdlib-jdk8"))
    testImplementation(gradleTestKit())
    testImplementation(libs.konf.yaml)
    testImplementation(libs.classgraph)
    testImplementation(libs.bundles.kotlin.testing)
}

sourceSets {
    main {
        resources {
            srcDir("src/main/sh")
        }
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(
            *org.gradle.api.tasks.testing.logging.TestLogEvent
                .values(),
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

signing {
    if (System.getenv()["CI"].equals("true", ignoreCase = true)) {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}

/*
 * Publication on Maven Central and the Plugin portal
 */
publishOnCentral {
    projectLongName.set(info.longName)
    projectDescription.set(description ?: TODO("Missing description"))
    projectUrl.set(info.website)
    scmConnection.set(info.scm)
    repository("https://maven.pkg.github.com/DanySK/${rootProject.name}".lowercase(), name = "github") {
        user.set("danysk")
        password.set(System.getenv("GITHUB_TOKEN"))
    }
    publishing {
        publications {
            withType<MavenPublication> {
                pom {
                    developers {
                        developer {
                            name.set("Danilo Pianini")
                            email.set("danilo.pianini@gmail.com")
                            url.set("http://www.danilopianini.org/")
                        }
                    }
                }
            }
        }
    }
}

gradlePlugin {
    website.set(info.website)
    vcsUrl.set(info.vcsUrl)
    plugins {
        create("gitHooks") {
            id = "$group.${project.name}"
            displayName = info.longName
            description = project.description
            implementationClass = info.pluginImplementationClass
            tags.set(info.tags)
        }
    }
}
