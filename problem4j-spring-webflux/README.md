# Problem4J Spring WebFlux

Spring WebFlux integration, for the reactive stack. It builds on [`problem4j-spring-web`](../problem4j-spring-web),
which resolves the framework and validation exceptions shared by both stacks, and adds the handling that only applies to
`spring-webflux`.

This is the module to depend on in a WebFlux application.

## Override `ProblemErrorWebExceptionHandler`

[`ProblemErrorWebExceptionHandler`][ProblemErrorWebExceptionHandler] replaces the default `spring-webflux` error
fallback, which inspects the `Accept` header to decide between a formatted error page and JSON built from
`ErrorAttributes`. The override always produces a `Problem` instead.

- Declare your own `ErrorWebExceptionHandler` bean to override it further.
- Exclude [`ProblemErrorWebFluxConfiguration`][ProblemErrorWebFluxConfiguration] to disable the override.

[ProblemErrorWebExceptionHandler]: src/main/java/io/github/problem4j/spring/webflux/ProblemErrorWebExceptionHandler.java

[ProblemErrorWebFluxConfiguration]: src/main/java/io/github/problem4j/spring/webflux/autoconfigure/ProblemErrorWebFluxConfiguration.java
