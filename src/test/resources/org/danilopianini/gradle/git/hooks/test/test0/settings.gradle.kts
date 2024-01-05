plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks")
}

gitHooks {
    file(".git").mkdirs()
    preCommit { tasks("ktlintCheck") }
    commitMsg { conventionalCommits() }
    createHooks()
}
