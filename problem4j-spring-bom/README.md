# Problem4J Spring BOM

[![Sonatype](https://img.shields.io/maven-central/v/io.github.problem4j/problem4j-spring-bom)](https://central.sonatype.com/artifact/io.github.problem4j/problem4j-spring-bom)

Bill of Materials (BOM) for the Spring integrations of **Problem4J**, a library implementing *RFC 7807 - Problem Details
for HTTP APIs*.

Importing this BOM lets you declare the individual `problem4j-*` modules **without repeating versions** and keeps all
components aligned.

Alongside the Spring modules published from this repository, it also constrains the modules of
[`problem4j-core`][problem4j-core], [`problem4j-gson`][problem4j-gson] and [`problem4j-jackson`][problem4j-jackson]:

| Module                     | Notes                                      |
|----------------------------|--------------------------------------------|
| `problem4j-core`           | `Problem` model and `ProblemException`     |
| `problem4j-gson`           | Gson serialization                         |
| `problem4j-jackson2`       | Jackson 2 serialization                    |
| `problem4j-jackson3`       | Jackson 3 serialization                    |
| `problem4j-spring-web`     | Shared core, pulled in by the two adapters |
| `problem4j-spring-webmvc`  | Servlet stack                              |
| `problem4j-spring-webflux` | Reactive stack                             |

Declare only what you actually use - in particular pick a single Jackson generation and a single web stack. The examples
below list every module just to show the shape.

## Using the BOM

### Gradle (Kotlin DSL)

Add the BOM to `implementation(platform(...))`, then declare modules without versions.

```kotlin
dependencies {
    implementation(platform("io.github.problem4j:problem4j-spring-bom:${version}"))

    implementation("io.github.problem4j:problem4j-core")
    implementation("io.github.problem4j:problem4j-gson")
    implementation("io.github.problem4j:problem4j-jackson2")
    implementation("io.github.problem4j:problem4j-jackson3")
    implementation("io.github.problem4j:problem4j-spring-web")
    implementation("io.github.problem4j:problem4j-spring-webmvc")
    implementation("io.github.problem4j:problem4j-spring-webflux")
}
```

### Maven

Add the BOM to `<dependencyManagement>` with `import` scope, then declare modules without versions.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.problem4j</groupId>
            <artifactId>problem4j-spring-bom</artifactId>
            <version>${version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.problem4j</groupId>
        <artifactId>problem4j-core</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.problem4j</groupId>
        <artifactId>problem4j-gson</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.problem4j</groupId>
        <artifactId>problem4j-jackson2</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.problem4j</groupId>
        <artifactId>problem4j-jackson3</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.problem4j</groupId>
        <artifactId>problem4j-spring-web</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.problem4j</groupId>
        <artifactId>problem4j-spring-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.problem4j</groupId>
        <artifactId>problem4j-spring-webflux</artifactId>
    </dependency>
</dependencies>
```

[problem4j-core]: https://github.com/problem4j/problem4j-core

[problem4j-gson]: https://github.com/problem4j/problem4j-gson

[problem4j-jackson]: https://github.com/problem4j/problem4j-jackson
