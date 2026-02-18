# Copilot Coding Agent Onboarding Guide for `problem4j-spring`

## Project Details

- **Repository:** `problem4j-spring`.
- **Purpose:** Spring Boot integrations for RFC 7807 "Problem Details" object, enabling consistent error responses for
  REST APIs using immutable `Problem` objects. Supports Spring WebMVC and WebFlux. Includes BOM for dependency.
  management and modules for each Spring integration.
- **Languages:** Java (main), Kotlin (build scripts).
- **Frameworks/Tools:** Spring Boot, Spring WebMVC, Spring WebFlux, Gradle (Kotlin DSL), JUnit Jupiter, AssertJ,
  Spotless, GitHub Actions CI.
- **Modules:**
    - `problem4j-spring-bom`: BOM for dependency alignment,
    - `problem4j-spring-web`: Core Spring integration,
    - `problem4j-spring-webmvc`: WebMVC overrides,
    - `problem4j-spring-webflux`: WebFlux overrides.
- **Java Version:** Java 17+.
- **Repo Size:** Medium (multiple modules, ~80+ source/test files per module).

## Build, Test, Lint, and Validation Steps

- **Bootstrap:** No explicit bootstrap step; dependencies managed via Gradle.
- **Build:**
    - Run `./gradlew` from the repository root  (or `./gradlew.bat` on Windows), default tasks are `spotlessApply build`
      so this is a convenience method.
    - Run `./gradlew build` only if you explicitly want to exclude code formatting for some reason.
    - Java 17+ required.
- **Test:**
    - Tests run automatically with `./gradlew build` or separately via `./gradlew test`.
    - Test files in `src/test/java` under each module.
- **Lint:**
    - Spotless check runs automatically on build. To manually lint/fix, use `./gradlew spotlessApply`.
    - Run `./gradlew spotlessCheck` to validate code style.
    - Run `./gradlew spotlessApply` to auto-format code.
    - Lint config in `build.gradle.kts`.
    - For limiting failures and noise, prefer running `./gradlew spotlessApply build` instead of just `./gradlew build`.
- **Clean:**
    - Run `./gradlew clean` to remove build artifacts.
- **Validation:**
    - CI/CD via GitHub Actions:
        - `.github/workflows/gradle-build.yml` (build/test),
        - `.github/workflows/gradle-dependency-submission.yml` (dependency graph),
        - `.github/workflows/gradle-publish-release.yml` (release).
    - All CI builds use JDK 17 and Gradle Wrapper.

## Project Layout & Key Files

- **Root Files:** `build.gradle.kts`, `settings.gradle.kts`, `README.md`, `RELEASING.md`, `gradlew`, `gradlew.bat`,
  `gradle/libs.versions.toml`, `.github/workflows/`.
- **Source Code:** `src/main/java` in each module.
- **Tests:** `src/test/java` in each module.
- **Build Scripts:** All modules have `build.gradle.kts`.
- **Build Utils:** Custom Gradle scripts in `buildSrc`.
- **Modules:**
  - `problem4j-spring-bom`: BOM for dependency management.
  - `problem4j-spring-web`: base integration for Spring Web.
  - `problem4j-spring-webmvc`: integration for Spring WebMVC.
  - `problem4j-spring-webflux`: integration for Spring WebFlux.
- **Spring entry points:** 
    - [`ProblemAutoConfiguration`](../problem4j-spring-web/src/main/java/io/github/problem4j/spring/web/autoconfigure/ProblemAutoConfiguration.java)
    - [`ProblemWebFluxAutoConfiguration`](../problem4j-spring-webflux/src/main/java/io/github/problem4j/spring/webflux/autoconfigure/ProblemWebFluxAutoConfiguration.java)
    - [`ProblemWebMvcAutoConfiguration`](../problem4j-spring-webmvc/src/main/java/io/github/problem4j/spring/webmvc/autoconfigure/ProblemWebMvcAutoConfiguration.java)

## Coding Guidelines

- Do not add self-explaining comments. Use comments only for clarity/context.
- Do not use wildcard imports.
- Always rely on `spotlessApply` task from Gradle for code formatting.
- Follow existing code patterns and naming conventions.
- Use Gradle tasks for build, test, and lint. Do not attempt manual compilation or test running.

## Unit Test Guidelines

- Name test methods using: `givenThis_whenThat_thenWhat`.
- Do not use comments like `// given`, `// when`, or `// then` to mark test sections; structure should be clear from the
  method body.
- Test both positive and negative cases for each feature or behavior.
- Prefer fluent assertion libraries such as AssertJ or Hamcrest.
- For tests in the `...integration` package of both `problem4j-spring-webflux` and `problem4j-spring-webmvc`, ensure
  they are similar, as both libraries should resolve the same exceptions to the same response bodies.

## Agent Instructions

- Trust these instructions for build, test, lint, and validation steps. Only search the codebase if information here is
  incomplete or incorrect.
- Prioritize changes in relevant module directories for core logic and tests.
- Always validate changes with a full build and test run before considering the task complete.

## Troubleshooting & Workarounds

- If build fails due to Java version, ensure Java 17+ is installed and selected.
- If Spotless fails, run `./gradlew spotlessApply` to auto-fix formatting.
- For dependency issues, check `gradle/libs.versions.toml` and run `./gradlew --refresh-dependencies`.
- For Windows, use `gradlew.bat` or Git Bash for shell compatibility with scripts.

## Additional Notes / Quick Reference

- Always run `./gradlew build` before pushing changes.
- Ensure all tests pass locally and that Spotless does not report errors.
- For publishing, set required environment variables and use the documented Gradle tasks.
- Check CI status on GitHub after pushing/PR.
- For further details, see `README.md` and `build.gradle.kts`. For CI/CD specifics, review workflow YAMLs in
  `.github/workflows/`.
