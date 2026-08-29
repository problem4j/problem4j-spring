# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][keepachangelog], and this project adheres to [Semantic Versioning][semver].

## [Unreleased]

### Added

- Make `AbstractProblemResolver` and its subclasses take additional arguments via `BeanPostProcessor` for easier
  overrides.
  > [!IMPORTANT]
  > When a resolver is registered as a bean, its `ProblemFormat` is replaced after construction by the container's
  > `ProblemFormat` bean, regardless of what was passed to a constructor. Do not rely on a per-resolver `ProblemFormat`
  > that differs from the container one.
- Add `*Aware` callback interfaces (`ProblemFormatAware`, `TypeNameMapperAware`, `BindingResultSupportAware`,
  `MethodValidationResultSupportAware`, `MethodParameterSupportAware`) that any bean can implement to receive the
  corresponding Problem4J collaborator from `ProblemBeanPostProcessor` after construction.

### Changed

- `ServerWebInputProblemResolver` no longer delegates type-mismatch handling to the `TypeMismatchProblemResolver` bean.

### Deprecated

- Deprecate non-default constructors of `AbstractProblemResolver` and its subclasses (those taking `ProblemFormat` or
  other collaborators), including `AbstractProblemResolver(Class, ProblemFormat)`. Collaborators are now injected after
  construction by `ProblemBeanPostProcessor`; use the no-arg constructor (`AbstractProblemResolver(Class)` for custom
  subclasses).

### Fixed

- Make `Automatic-Module-Name` stable, by adding it to `META-INF/MANIFEST.MF`.
- Generate `META-INF/spring-autoconfigure-metadata.properties` file with `spring-boot-autoconfigure-processor`.
- Make `HttpStatusTitleResolver` implement `Serializable` as it is used in a field of `ProblemBuilder`.
- Resolve response `Content-Type` based on `Accept` header, defaulting to `application/problem+json`.

## [3.0.0] - 2026-05-08

### Added

- Add `HttpStatusTitleResolver` - a SPI implementation fo `StatusTitleResolver` using Spring's `HttpStatus`.
- Add `problem4j.title-override` property to configure `title` field override in `DefaultProblemPostProcessor`.
- Make `DefaultProblemPostProcessor` support any fields from `ProblemContext` in value interpolation.
- Add `@FunctionalInterface` contract to:
  - `MethodParameterSupport`,
  - `MethodValidationResultSupport`,
  - `BindingResultSupport`,
  - `ProblemResolverStore`,
  - `ProblemPostProcessor`,
  - `TypeNameMapper`,
  - `AdviceWebFluxInspector`,
  - `AdviceWebMvcInspector`.

### Changed

- Bump `problem4j-core` to `2.0.0`.
- Bump `problem4j-jackson2` to `2.0.0`.
- Bump `problem4j-jackson3` to `2.0.0`.
- Change `ProblemResolver` to contain just `resolve` method, returning `Problem`.
- Hide `IdentityProblemFormat` and make it available via `ProblemFormat.identity()`.
- Hide `IdentityProblemPostProcessor` and make it available via `ProblemPostProcessor.identity()`.
- Move `ProblemSupport` into `ViolationSupport` to mitigate conflicts with `ProblemSupport` in `problem4j-core`.
- Simple tracing now produces 32-character, lowercase-hex string.

### Removed

- Remove `problem4j.resolver-caching.max-cache-size` and evicting cache for `ProblemResolver`-s (non-evicting cache is
  still present).
- Remove `ProblemSupport.resolveStatus(HttpStatusCode)`.
- Remove `ProblemSupport.resolveStatus(Problem)`.

## [2.2.4] - 2026-03-29

### Fixed

- Fix formatting of `detailFormat` for resolving cause exceptions.
- Drop transitive dependency to `kotlin-stdlib` (added accidentally by Kotlin plugin, used in **tests** only).

## [2.2.3] - 2026-03-26

### Fixed

- Fix detail about enum deserialization errors to correctly say `"enum"` instead of `"string"`.

## [2.2.2] - 2026-03-16

## Changed

- Deprecate `ProblemSupport.resolveProblem`.
- Bump `problem4j-core` to `1.4.3`.
- Bump `problem4j-jackson2` to `1.4.3`.
- Bump `problem4j-jackson3` to `1.4.3`.

## Fixed

- Fix missing details about primitive types deserialization errors. It will include `"detail": "Type mismatch"` with
  `"property"` and `"kind"` extensions containing type property and its expected type.
- Document configuration properties via JavaDocs and hints in `additional-spring-configuration-metadata.json` for
  annotation processor to generate an improved.

## [2.2.1] - 2026-02-26

### Changed

- Bump `problem4j-core` to `1.4.2`.
- Bump `problem4j-jackson2` to `1.4.2`.
- Bump `problem4j-jackson3` to `1.4.2`.

## [2.2.0] - 2026-02-19

### Added

- Add support for JSpecify annotations for nullability and Kotlin interop.

### Changed

- Bump `problem4j-core` to `1.4.1`.
- Bump `problem4j-jackson2` to `1.4.1`.
- Bump `problem4j-jackson3` to `1.4.1`.

### Fixed

- Resolve minor null-safety issues for handling nullable `ProblemContext` fields.

## [2.1.3] - 2026-02-12

### Changed

- Bump `problem4j-core` to `1.3.3`.
- Bump `problem4j-jackson2` to `1.3.3`.
- Bump `problem4j-jackson3` to `1.3.3`.

## [2.1.2] - 2026-01-31

### Changed

- Bump `problem4j-core` to `1.3.2`.
- Bump `problem4j-jackson2` to `1.3.2`.
- Bump `problem4j-jackson3` to `1.3.2`.

### Fixed

- Hide nested configuration class (`ProblemXmlMapperConfiguration`) that was accidentally released as `public`.

## [2.1.1] - 2026-01-16

### Added

- Finalize missing JavaDocs (and fix various existing) - all `public` classes and methods now have proper JavaDocs.

### Changed

- Bump `problem4j-core` to `1.3.1`.
- Bump `problem4j-jackson2` to `1.3.1`.
- Bump `problem4j-jackson3` to `1.3.1`.

### Fixed

- Hardcode value of `PROBLEM_CONTEXT_ATTRIBUTE` const to make it possible to use in annotation.

## [2.1.0] - 2025-12-24

Versions `2.0.x` were released in sandbox namespace and are ignored in this `CHANGELOG.md`. Version `2.1.0` is the first
public release for Problem4J's integration with Spring Boot 4.

### Changed

- Integrate with **Spring Boot to `4.0.0`** (and by extension with Spring Framework to `7.0.1`).
- Integrate with Jackson 3 via `problem4j-jackson3`.
- Enable backwards compatibility with Jackson 2.x using `@ConditionalOnClass` (so you need to add `spring-boot-jackson2`
  module manually).

## [1.2.9] - 2026-03-29

### Fixed

- Fix formatting of `detailFormat` for resolving cause exceptions.

## [1.2.8] - 2026-03-26

### Fixed

- Fix detail about enum deserialization errors to correctly say `"enum"` instead of `"string"`.

## [1.2.7] - 2026-03-16

### Changed

- Deprecate `ProblemSupport.resolveProblem`.
- Bump `problem4j-core` to `1.4.3`.
- Bump `problem4j-jackson2` to `1.4.3`.

### Fixed

- Fix missing details about primitive types deserialization errors. It will include `"detail": "Type mismatch"` with
  `"property"` and `"kind"` extensions containing type property and its expected type.
- Document configuration properties via JavaDocs and hints in `additional-spring-configuration-metadata.json` for
  annotation processor to generate an improved.

## [1.2.6] - 2026-02-26

### Changed

- Bump `problem4j-core` to `1.4.2`.
- Bump `problem4j-jackson2` to `1.4.2`.

## [1.2.5] - 2026-02-17

### Changed

- Bump `problem4j-core` to `1.4.1`.
- Bump `problem4j-jackson2` to `1.4.1`.

### Fixed

- Resolve minor null-safety issues for handling nullable `ProblemContext` fields.

## [1.2.4] - 2026-02-17

### Changed

- Bump `problem4j-core` to `1.4.0`.
- Bump `problem4j-jackson2` to `1.4.0`.

## [1.2.3] - 2026-02-12

### Changed

- Bump `problem4j-core` to `1.3.3`.
- Bump `problem4j-jackson2` to `1.3.3`.

## [1.2.2] - 2026-01-31

### Changed

- Bump `problem4j-core` to `1.3.2`.
- Bump `problem4j-jackson2` to `1.3.2`.

## [1.2.1] - 2026-01-16

### Added

- Finalize missing JavaDocs (and fix various existing) - all `public` classes and methods now have proper JavaDocs.

### Changed

- Bump `problem4j-core` to `1.3.1`.
- Bump `problem4j-jackson2` to `1.3.1`.

## [1.2.0] - 2025-12-24

This release of `problem4j-spring` is considered a first "public" release, so the entry aggregates changes from the
`v1.0.x` to `v1.1.x` release lines into single entry.

### Added

- Add resolving `ProblemExceptions`-s into `application/problem+json` objects.
- Add resolving `@ProblemMapping`-annotated exceptions into `application/problem+json` objects.
- Add `ProblemResolver` interface for integration of exceptions that cannot be annotated or inherited from
  `ProblemException`.
- Add basic support for Spring's build-in `@ResponseStatus` annotation.
- Override response body for all Spring build-in exceptions to be minimalistic `Problem` responses.
- Add configurable option for response error tracing.
- Add overriding of `"type"` and `"instance"` field in `ProblemPostProcessor` to customize resolvable URIs.
- Add customizing `"detail"` field format of build-in exceptions (`lowercase`, `capitalized` or `uppercase`)
- Add `AdviceMvcInspector` and `AdviceWebFluxInspector` to peek on `Problem` objects before returning a response (in
  general for logging purposes).
- Enable loading library with Spring Boot Auto-configuration.

[keepachangelog]: https://keepachangelog.com/en/1.1.0/

[semver]: https://semver.org/spec/v2.0.0.html
