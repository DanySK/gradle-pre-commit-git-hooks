# Gradle pre-commit Git Hooks

A Gradle plugin enforcing pre-commit and commit-msg Git hooks configuration. Conventional-commits-ready.

Hooks are created upfront, before the execution of any task.
This implies that even when importing the project into a gradle-aware IDE,
the hooks are generated automatically.

## Usage

This plugin must be applied to `settings.gradle.kts`.
Groovy syntax not supported.

```kotlin
plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "<version>"
}

gitHooks {
    // Configuration
    createHooks() // actual hooks creation
}
```

### Contexts

The plugin has special support for `commit-msg` and `pre-commit` hooks, which are the ones most frequently used.
Conventional commits are enforced at the `commit-msg` level.
Custom hooks can still be specified.

```kotlin
gitHooks {
    preCommit {
        // Configuration for pre-commit
    }
    hook("pre-merge-commit") {
        // Configuration for pre-merge-commit
    }
    hook("foo") {
        // Configuration for foo
    }
    commitMsg {
        // Configuration, with conventional commits support
    } 
    createHooks()
}
```

### Configuration options available to every hook

Every script can be initialized by writing its code in form of `String`
or by downloading it from a `URL`.
Resource loading from classpath returns `URL`s and can thus be used transparently.
These cases require the method `from`.

The script can be enriched with more code by calling `appendScript`.

Finally, the script can be required to run gradle tasks,
either requiring their success or not, through `tasks`

```kotlin
gitHooks {
    path = "some/folder" // custom git repository location, defaults to the local project and scans the parents 
    preCommit {
        // Script downloaded from a source, can be a String or URL
        from("https://my.repo/pre-commit.sh")
        // Content can be added at the bottom of the script
        appendScript {
            """
            echo additional lines
            """
        }
    }
    hook("pre-merge-commit") {
        from { // Creates a fresh script with a bash shebang line
            """
            echo some bash code
            """
        }
        // Content can be added at the bottom of the script
        appendScript {
            """
            echo additional lines
            """
        }
    }
    hook("foo") {
        from("#!/usr/bin/env ruby") { // Custom shebang line
            """
            puts "Hello, world"
            """
        }
        tasks("baz", "gra") // also runs tasks baz and gra, requiring their success
    }
    hook("baz") {
        tasks("zin", "pla", requireSuccess = false) // runs tasks zin and pla, ignoring failure
        tasks("wab") // then, runs task wab, requiring success
    }
    createHooks()
}
```

### Conventional commits

Conventional commits receive special treatment,
they can get configured succinctly inside the `commitMsg` block.

The Default configuration supports the following commit types:
`fix`, `feat`, `build`, `chore`, `ci`, `docs`, `perf`, `refactor`, `revert`, `style`, `test`.

```kotlin
gitHooks {
    commitMsg { conventionalCommits() } // Applies the default conventional commits configuration
    createHooks()
}
```

The base configuration supports only `fix` and `feat`

```kotlin
gitHooks {
    commitMsg {
        conventionalCommits { } // Only feat and fix
    }
    createHooks()
}
```

The following shows a custom configuration,
adding custom types `foo`, `bar`, and `baz` to `fix` and `feat`

```kotlin
gitHooks {
    commitMsg {
        conventionalCommits {
            types("foo", "bar") 
            types("baz")
        }
    }
    createHooks()
}
```

The following custom configuration instead extends the default configuration,
adding custom types `foo` and `bar`.

```kotlin
gitHooks {
    commitMsg {
        conventionalCommits {
            defaultTypes()
            types("foo", "bar") 
        }
    }
    createHooks()
}
```
