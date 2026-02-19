import internal.isTestTask
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("internal.java-convention")
    id("net.ltgt.errorprone")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        if (isTestTask()) {
            disableAllChecks = true
        }

        error("NullAway")
        option("NullAway:OnlyNullMarked", "true")
        option("NullAway:JSpecifyMode", "true")
    }
}
