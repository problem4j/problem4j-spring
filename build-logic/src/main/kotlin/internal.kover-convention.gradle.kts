plugins {
    id("java")
    id("org.jetbrains.kotlinx.kover")
}

tasks.named<Task>("check").configure {
    finalizedBy(tasks.named("koverHtmlReport"), tasks.named("koverXmlReport"))
}
