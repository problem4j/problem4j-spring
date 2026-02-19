package internal

// This file contains extension functions for Gradle's Task API.

import org.gradle.api.Task

/**
 * Determines whether this task is a test task. For simplicity, a task is considered a test task if
 * its name contains "test" (case-insensitive).
 *
 * @return `true` if the task name matches a test task pattern, `false` otherwise.
 */
fun Task.isTestTask(): Boolean = name.matches(Regex(".*[tT]est.*"))
