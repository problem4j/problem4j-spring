<h1 align="center">Problem4J Spring</h1>

<p align="center">
  <a href="https://codecov.io/gh/problem4j/problem4j-spring"><img src="https://codecov.io/gh/problem4j/problem4j-spring/graph/badge.svg?token=S2DOKHIXMH" alt="Codecov"></a>
  <a href="https://github.com/problem4j/problem4j-spring/actions/workflows/ci.yml"><img src="https://github.com/problem4j/problem4j-spring/actions/workflows/ci.yml/badge.svg" alt="Build Status"></a>
  <a href="https://central.sonatype.com/artifact/io.github.problem4j/problem4j-spring-bom"><img src="https://img.shields.io/maven-central/v/io.github.problem4j/problem4j-spring-bom?filter=!*-*" alt="Sonatype"></a>
  <a href="https://github.com/problem4j/problem4j-spring/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-Apache%202.0-green" alt="License"></a>
</p>

Designing clear and consistent error responses in a REST API is often harder than it looks. Without a shared standard,
each application ends up inventing its own ad-hoc format, which quickly leads to inconsistency and confusion.
[RFC 7807 - Problem Details for HTTP APIs][rfc7807] solves this by defining a simple, extensible JSON structure for
error messages.

**Problem4J** brings this specification into the Spring ecosystem, offering a practical way to model, throw, and handle
API errors using `Problem` objects. It helps you enforce a consistent error contract across your services, while staying
flexible enough for custom exceptions and business-specific details.

> Note that [RFC 7807][rfc7807] was later extended in [RFC 9457][rfc9457], however core concepts remain the same.

## Table of Contents

- [Why bother with Problem4J](#why-bother-with-problem4j)
- [Usage](#usage)
- [Maven Dependency](#maven-dependency)
- [Repository](#repository)
- [Project Status](#project-status)
- [Problem4J Links](#problem4j-links)
- [Building from source](#building-from-source)

## Why bother with Problem4J

Spring already supports [**RFC 7807**][rfc7807] with `ProblemDetail`, `ErrorResponse` and
`ResponseEntityExceptionHandler`. It works well for framework exceptions and for exceptions written as `ErrorResponse`,
but leaves a few gaps that **Problem4J** fills:

- **Domain exceptions without boilerplate.** In Spring, each custom exception has to extend `ErrorResponseException`
  (or implement `ErrorResponse`), or needs its own `@ExceptionHandler`. Problem4J maps exceptions declaratively with
  `@ProblemMapping` - with `type`, `title`, `detail` and extensions interpolated from exception fields and request
  context (e.g. `{context.traceId}`) - or imperatively with `ProblemException`.
- **Actionable details for framework errors.** Spring's built-in responses use generic details (e.g. `Invalid request
  content.`) and do not tell which field or parameter was wrong. Problem4J adds structured extensions, such as `errors`
  with field violations, or `param`, `header` and `property` naming the invalid input - without exposing raw exception
  messages.
- **One place for cross-cutting rules.** Application properties can override `type`, `title` and `instance` with
  templates, and inject a trace ID from a request header into every response.
- **Immutable, framework-independent model.** `Problem` is immutable (unlike `ProblemDetail`) and comes from
  [`problem4j-core`][problem4j-core], so the same model and JSON mapping (Jackson, Gson) can be used outside Spring,
  e.g. in API clients.
- **Same behavior in WebMVC and WebFlux**, auto-configured for Spring Boot. Every component can be replaced
  (`@ConditionalOnMissingBean`) or disabled via properties, and existing `ErrorResponse` exceptions keep working.

## Usage

Extensive usage manual explaining library features can be found on [GitHub Pages][github-pages].

The primary ways to produce a `Problem` response are:

1. Throwing a `ProblemException` with a manually built `Problem`.
2. Annotating an exception class with `@ProblemMapping`.
3. Implementing a custom `ProblemResolver`.
4. Registering a `ProblemResolver` via the Kotlin DSL.

### 1. Throwing a `ProblemException`

```java
throw new ProblemException(Problem.of("Invalid Request", 400, "not a valid json"));
```

It would produce following response with `application/problem+json`.

```json
{
    "title": "Invalid Request",
    "status": 400,
    "detail": "not a valid json"
}
```

### 2. Using `@ProblemMapping` on a custom exception

```java
@ProblemMapping(
    type = "errors/invalid-request",
    title = "Invalid Request",
    status = 400,
    detail = "{message}: {fieldName}",
    extensions = {"userId", "fieldName"})
public class ExampleException extends RuntimeException {

  private final String userId;
  private final String fieldName;

  public ExampleException(String userId, String fieldName) {
    super("bad input for user " + userId);
    this.userId = userId;
    this.fieldName = fieldName;
  }
}
```

It would produce following response with `application/problem+json`.

```json
{
    "type": "errors/invalid-request",
    "title": "Invalid Request",
    "status": 400,
    "detail": "bad input for user u-123: age",
    "userId": "u-123",
    "fieldName": "age"
}
```

### 3. Implementing a custom `ProblemResolver`

```java
@Component
public class ExampleExceptionResolver implements ProblemResolver {

  @Override
  public Class<? extends Exception> getExceptionClass() {
    return ExampleException.class;
  }

  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return Problem.builder()
        .type("errors/invalid-request")
        .title("Invalid Request")
        .status(400)
        .detail("bad input for user " + ((ExampleException) ex).getUserId())
        .extension("userId", ((ExampleException) ex).getUserId())
        .build();
  }
}
```

It would produce following response with `application/problem+json`.

```json
{
    "type": "errors/invalid-request",
    "title": "Invalid Request",
    "status": 400,
    "detail": "bad input for user u-456",
    "userId": "u-456"
}
```

### 4. Registering a `ProblemResolver` via the Kotlin DSL

> **Since `3.1.0`.**

```kotlin
@Configuration
class ExampleExceptionResolverConfiguration {

  @Bean
  fun exampleProblemResolver(): ProblemResolver =
      problemResolver<ExampleException> { ex ->
        type("errors/invalid-request")
        title("Invalid Request")
        status(400)
        detail("bad input for user ${ex.userId}")
        extension("userId", ex.userId)
      }
}
```

It would produce the same response as the resolver above.

## Maven Dependency

Add library as dependency to Maven or Gradle. See the actual versions on [Maven Central][maven-central]. Add it along
with repository in your dependency manager. **Java 17** or higher is required to use this library.

It's assumed that Spring Boot will be already present in your project dependencies, as `problem4j-spring` is only an
extension to it and does not bring it transitively.

1. Maven:
   ```xml
   <dependencies>
       <!-- pick the one for your project -->
       <dependency>
           <groupId>io.github.problem4j</groupId>
           <artifactId>problem4j-spring-webflux</artifactId>
           <version>${version}</version>
       </dependency>
       <dependency>
           <groupId>io.github.problem4j</groupId>
           <artifactId>problem4j-spring-webmvc</artifactId>
           <version>${version}</version>
       </dependency>
   </dependencies>
   ```
2. Gradle (Kotlin DSL):
   ```groovy
   dependencies {
       // pick the one for your project
       implementation("io.github.problem4j:problem4j-spring-webflux:${version}")
       implementation("io.github.problem4j:problem4j-spring-webmvc:${version}")
   }
   ```

## Repository

This project contains three major versions, dedicated to Spring Boot 3 and 4.

| branch           | info                                                       | latest                                                                                                                   |
|------------------|------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| `main`           | `v3.x` for Spring Boot `4.x`, based on `problem4j-core-v2` | [![Sonatype](https://img.shields.io/maven-central/v/io.github.problem4j/problem4j-spring-bom?filter=3.*)][maven-central] |
| `release-v2.*.x` | `v2.x` for Spring Boot `4.x`, based on `problem4j-core-v1` | [![Sonatype](https://img.shields.io/maven-central/v/io.github.problem4j/problem4j-spring-bom?filter=2.*)][maven-central] |
| `release-v1.*.x` | `v1.x` for Spring Boot `3.x`, based on `problem4j-core-v1` | [![Sonatype](https://img.shields.io/maven-central/v/io.github.problem4j/problem4j-spring-bom?filter=1.*)][maven-central] |

## Project Status

[![Status: Feature Complete](https://img.shields.io/badge/feature%20complete-darkblue?label=status)](#project-status)

**Problem4J Spring** is considered *feature complete*. Only **bug fixes** will be added. New features may be included
only if there is a strong justification for them; otherwise, future projects are expected to build on this one as a
dependency.

> There are still some aspects from Spring Framework that are not covered at the moment (such as Spring Security for
> example). It's possible however that instead of libraries, an example project will be created that showcases how to
> set `Problem` handling in that modules without external dependency.

## Problem4J Links

- [`problem4j.github.io`](https://problem4j.github.io) - Full documentation of all projects from Problem4J family.
- [`problem4j-core`][problem4j-core] - Core library defining `Problem` model and `ProblemException`.
- [`problem4j-jackson`][problem4j-jackson] - Jackson module for serializing and deserializing `Problem` objects.
- [`problem4j-spring`][problem4j-spring] - Spring modules extending `ResponseEntityExceptionHandler` for handling
  exceptions and returning `Problem` responses.

## Building from source

<details>
<summary><b>Expand...</b></summary>

**Note** that **Java 21+** is recommended for local development.

The produced artifacts are compatible with **Java 17** thanks to `options.release = 17` in Gradle `JavaCompile` tasks.
This means that regardless of the Java version used to run Gradle, the resulting bytecode remains compatible.

The **default Gradle tasks** include `spotlessApply` (for code formatting) and `build` (for compilation and tests).The
simplest way to build the project is to run:

```bash
./gradlew
```

---

To **execute tests** use `test` task. Tests do not change `options.release` so newer Java API can be used. 

```bash
./gradlew test
```

---

To **format the code** according to the style defined in [`build.gradle.kts`](./build.gradle.kts) rules use `spotlessApply`
task. **Note** that **building will fail** if code is not properly formatted.

```bash
./gradlew spotlessApply
```

To **publish** the built artifacts to **local Maven repository**, use `publishToMavenLocal` task.

```bash
./gradlew publishToMavenLocal
```

Note that for using Maven Local artifacts in target projects, you need to add `mavenLocal()` repository.

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}
```

</details>

[github-pages]: https://problem4j.github.io

[maven-central]: https://central.sonatype.com/namespace/io.github.problem4j

[problem4j-core]: https://github.com/problem4j/problem4j-core

[problem4j-jackson]: https://github.com/problem4j/problem4j-jackson

[problem4j-spring]: https://github.com/problem4j/problem4j-spring

[rfc7807]: https://datatracker.ietf.org/doc/html/rfc7807

[rfc9457]: https://datatracker.ietf.org/doc/html/rfc9457
