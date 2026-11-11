# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][keepachangelog], and this project adheres to [Semantic Versioning][semver].

## [Unreleased]

### Added

- Make `AbstractProblemResolver` and its subclasses take additional arguments via `BeanPostProcessor` for easier
  overrides. When a resolver is registered as a bean, its collaborators (e.g. `ProblemFormat`) are replaced after
  construction by the container's beans. For backwards compatibility with `3.0.0`, collaborators passed explicitly to
  a (deprecated) constructor are kept and never replaced.
- Add `*Aware` callback interfaces (`ProblemFormatAware`, `TypeNameMapperAware`, `BindingResultSupportAware`,
  `MethodValidationResultSupportAware`, `MethodParameterSupportAware`) that any bean can implement to receive the
  corresponding Problem4J collaborator from `ProblemBeanPostProcessor` after construction.
- Add `SimpleTypeNameMapper(TypeNameEntry...)` constructor for custom type mappings, compared in given order before the
  default ones (each matching its type and subtypes), and `simpleTypeNameMapper(...)` Kotlin function creating it from
  `type to name` pairs.
- Add `problem4j-gson` and register `ProblemTypeAdapterFactory` if present on the classpath.
- Add Kotlin extensions and DSL functions for common Problem4J classes and utilities.
- Add `problem4j.duplicate-resolver-policy` property to control what happens when multiple `ProblemResolver` instances
  for the same exception class have equal order. `fail` (default) fails application startup, `first` picks the first
  one registered in the application context, `last` picks the last one registered in the application context (so a
  built-in resolver wins over an application-defined one, similarly to previous versions).
- Register Problem4J autoconfigurations in Spring Boot test slices: `@AutoConfigureJson`, `@AutoConfigureWebFlux` and
  `@AutoConfigureWebMvc`, to make it possible to use `@JsonTest`, `@WebFluxTest`, `@WebMvcTest`, `@RestClientTest` and
  `@WebClientTest`.
- Add `problem4j.tracing-source` property deciding where `traceId` of `ProblemContext` comes from. `micrometer`
  registers `MicrometerProblemContextWebMvcFilter` / `MicrometerProblemContextWebFluxFilter`, which take `traceId` and
  `spanId` of the current Micrometer Tracing span, falling back to `problem4j.tracing-header-name` header or a random
  trace ID if no span is available. `micrometer` requires `io.micrometer:micrometer-tracing` on the classpath, otherwise
  application startup fails. `simple` keeps the previous behavior (header or a random trace ID). `auto` (default)
  uses `micrometer` if `io.micrometer:micrometer-tracing` is on the classpath, and `simple` otherwise.
- Add `ProblemContextWebMvcArgumentResolver` and `ProblemContextWebFluxArgumentResolver`, resolving controller method
  arguments of `ProblemContext` type without any annotation, to the `ProblemContext` assigned to the current request by
  `ProblemContextWebMvcFilter` / `ProblemContextWebFluxFilter` (`null` if none is assigned).
- Add `problem4j.extension-overrides.<name>` properties for overriding extension members of `Problem` responses with
  `DefaultProblemPostProcessor`. Templates may use `{problem.type}`, `{problem.title}`, `{problem.status}`,
  `{problem.detail}`, `{problem.instance}` and `{context.<key>}` placeholders. An override replaces an existing
  extension with the same name and is skipped if any placeholder cannot be resolved. A template made of exactly one
  placeholder keeps the type of the resolved value (e.g. `{problem.status}` is a number).
- Add `ObservationErrorWebMvcInspector` and `ObservationErrorWebFluxInspector`, recording exceptions handled by
  Problem4J as errors of the current HTTP server observation (`ServerRequestObservationContext`), so that
  `http.server.requests` metrics and spans report the `exception` instead of `none`. Add
  `problem4j.observation-errors` property deciding which exceptions are recorded. `server` (default) records
  exceptions resulting in a 5xx response, `all` records all of them, `none` disables recording.

### Changed

- With `io.micrometer:micrometer-tracing` on the classpath, `traceId` of `ProblemContext` comes from the current tracing
  span by default (`problem4j.tracing-source=auto`), instead of the `problem4j.tracing-header-name` header or a random
  trace ID. The header value is used only if no span is available, and the header is still written into responses with
  the span's `traceId`. Set `problem4j.tracing-source=simple` to keep the previous behavior.
- Exceptions handled by Problem4J resulting in a 5xx response are recorded as errors of the current HTTP server
  observation by default (`problem4j.observation-errors=server`), so the `exception` key of
  `http.server.requests` metrics and spans contains the exception class name instead of `none`. Set
  `problem4j.observation-errors=none` to keep the previous behavior.
- Bump `problem4j-core` to `2.0.1`.
- `ServerWebInputProblemResolver` no longer delegates type-mismatch handling to the `TypeMismatchProblemResolver` bean.
- Split `ProblemAutoConfiguration` into several auto-configurations. `ProblemAutoConfiguration` now keeps only generic
  beans (`ProblemMapper`, `ProblemFormat`, `ProblemPostProcessor`, `TypeNameMapper`, `ProblemBeanPostProcessor`) and no
  longer requires a web application. Web-related beans (`ProblemResolverStore`, built-in `ProblemResolver`s and
  parameter supports) moved to `ProblemWebAutoConfiguration`. Serialization beans moved to
  `ProblemJacksonAutoConfiguration` (Jackson 3 JSON, Jackson 3 XML, Jackson 2) and `ProblemGsonAutoConfiguration`, which
  also no longer require a web application, so `Problem` serialization is configured in non-web applications too (e.g.
  ones consuming Problem responses via HTTP client).
- `ProblemResolver` instances for the same exception class are picked based on `@Order` or `Ordered` (lower value wins).
  If multiple candidates are present with the same order, the startup fails with an error (unless
  `problem4j.duplicate-resolver-policy` is set to `first` or `last`).
- `PostProcessorSettings` methods now have default implementations returning no override (`null` for `type`, `title` and
  `instance`, empty map for extensions), so custom implementations only need to override the settings they use.
- `ProblemContextSettings.getTracingHeaderName()` now has a default implementation returning `null`. As the interface no
  longer has an abstract method, it can't be implemented with a lambda anymore - use an anonymous class instead.

### Deprecated

- Deprecate non-default constructors of `AbstractProblemResolver` and its subclasses (those taking `ProblemFormat` or
  other collaborators), including `AbstractProblemResolver(Class, ProblemFormat)`. Collaborators are now injected after
  construction by `ProblemBeanPostProcessor`; use the no-arg constructor (`AbstractProblemResolver(Class)` for custom
  subclasses).

### Fixed

- Make `Automatic-Module-Name` stable, by adding it to `META-INF/MANIFEST.MF`.
- Generate `META-INF/spring-autoconfigure-metadata.properties` file with `spring-boot-autoconfigure-processor`.
- Make `HttpStatusTitleResolver` implement `Serializable` as it is used in a field of `ProblemBuilder`.

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
