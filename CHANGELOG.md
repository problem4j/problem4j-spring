# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][keepachangelog], and this project adheres to [Semantic Versioning][semver].

## [Unreleased]

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
- Add resolving `@ProblemStatus`-annotated exceptions into `application/problem+json` objects.
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
