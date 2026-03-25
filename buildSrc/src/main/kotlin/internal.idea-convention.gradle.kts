import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.JUnit
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    id("org.jetbrains.gradle.plugin.idea-ext")
}

idea {
    project {
        settings {
            runConfigurations {
                create<Gradle>("Clean [problem4j-spring]") {
                    taskNames = listOf("clean")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<Gradle>("Build [problem4j-spring]") {
                    taskNames = listOf("spotlessApply build")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<Gradle>("Format Code [problem4j-spring]") {
                    taskNames = listOf("spotlessApply")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<JUnit>("JUnit [problem4j-spring-web]") {
                    moduleName = "problem4j-spring.problem4j-spring-web.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.problem4j.spring.web"
                }
                create<JUnit>("JUnit [problem4j-spring-webflux]") {
                    moduleName = "problem4j-spring.problem4j-spring-webflux.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.problem4j.spring.webflux"
                }
                create<JUnit>("JUnit [problem4j-spring-webmvc]") {
                    moduleName = "problem4j-spring.problem4j-spring-webmvc.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.problem4j.spring.webmvc"
                }
            }
        }
    }
}
