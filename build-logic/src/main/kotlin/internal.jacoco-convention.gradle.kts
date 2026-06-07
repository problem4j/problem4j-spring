plugins {
    id("java")
    id("jacoco")
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}

tasks.named<Task>("check") {
    finalizedBy(tasks.named("jacocoTestReport"))
}
