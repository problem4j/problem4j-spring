plugins {
    id("java")
    id("jacoco")
}

tasks.named<JacocoReport>("jacocoTestReport").configure {
    dependsOn(tasks.named("test"))

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }

    classDirectories.setFrom(
        classDirectories.files.map { fileTree(it) { exclude("**/*Kt.class") } },
    )
}

tasks.named<Task>("check").configure {
    finalizedBy(tasks.named("jacocoTestReport"))
}
