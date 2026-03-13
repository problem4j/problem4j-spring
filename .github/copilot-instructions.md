# Copilot Instructions - `problem4j-spring`

Spring Boot integrations for RFC 7807 "Problem Details" - consistent error responses for REST APIs using immutable
`Problem` objects. Supports Spring WebMVC and WebFlux.

## Modules

| Module                     | Purpose                      | Entry point                                                                                                                                                          |
|----------------------------|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `problem4j-spring-bom`     | BOM for dependency alignment | -                                                                                                                                                                    |
| `problem4j-spring-web`     | Core Spring integration      | [`ProblemAutoConfiguration`](../problem4j-spring-web/src/main/java/io/github/problem4j/spring/web/autoconfigure/ProblemAutoConfiguration.java)                       |
| `problem4j-spring-webmvc`  | WebMVC overrides             | [`ProblemWebMvcAutoConfiguration`](../problem4j-spring-webmvc/src/main/java/io/github/problem4j/spring/webmvc/autoconfigure/ProblemWebMvcAutoConfiguration.java)     |
| `problem4j-spring-webflux` | WebFlux overrides            | [`ProblemWebFluxAutoConfiguration`](../problem4j-spring-webflux/src/main/java/io/github/problem4j/spring/webflux/autoconfigure/ProblemWebFluxAutoConfiguration.java) |

Prioritize changes in the module matching the Spring integration in context.

## Build & Validate

Requires **JDK 17+**. Dependencies managed in `gradle/libs.versions.toml`. Custom Gradle plugins live in `buildSrc`.

```shell
./gradlew                  # default: spotlessApply build (preferred)
./gradlew spotlessApply    # auto-format code
./gradlew build            # compile + test + spotlessCheck
./gradlew test             # tests only
```

Always validate changes with a full build and test run before considering the task complete.

## Agent Rules

- Do not use terminal commands (e.g., `cat`, `find`, `ls`) to read or list project files - use IDE/agent tools instead.

## Coding Rules

- No self-explaining comments - only add comments for non-obvious context.
- No wildcard imports.
- Follow existing code patterns and naming conventions.
- Let `spotlessApply` handle all formatting - never format manually.

## Test Conventions

- Method naming: `givenThis_whenThat_thenWhat`.
- No `// given`, `// when`, `// then` section comments.
- Cover both positive and negative cases.
- Use AssertJ for assertions.
- Integration tests in `problem4j-spring-webflux` and `problem4j-spring-webmvc` (`...integration` package) should be
  similar - both modules must resolve the same exceptions to the same response bodies.
- If executing, run tests once, save output to `build/test-run.log` inside the repo (`> build/test-run.log 2>&1`), then
  read from that file to extract errors. Never run the same test command multiple times, without changes in sources. You
  can store test output in multiple files if you want to compare before/after changes (ex. `build/test-run-{i}.log`).
