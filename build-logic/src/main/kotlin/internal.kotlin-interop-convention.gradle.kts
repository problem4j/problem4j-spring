import internal.getIntProperty
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
}

val javaToolchainVersion = project.getIntProperty("internal.java.toolchain-version")
val javaTargetVersion = project.getIntProperty("internal.java.target-version")

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(javaToolchainVersion)
    }
    compilerOptions {
        javaParameters = true
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2
    }
}

tasks.named<KotlinCompile>("compileKotlin").configure {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaTargetVersion.toString())
    }
}
