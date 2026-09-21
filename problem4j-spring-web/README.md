# Problem4J Spring Web

Stack-agnostic core of the Spring integration - the shared foundation of
[`problem4j-spring-webmvc`](../problem4j-spring-webmvc) and [`problem4j-spring-webflux`](../problem4j-spring-webflux),
holding everything that is not tied to a specific Spring web stack.

You normally **do not depend on this module directly**. Pick the adapter for your application and this module comes with
it transitively.

## What lives here

- **Resolution** - [`ProblemResolver`][ProblemResolver] and [`AbstractProblemResolver`][AbstractProblemResolver], the
  `Exception`-to-`Problem` contract.
- **Resolver lookup** - [`ProblemResolverStore`][ProblemResolverStore], with a caching implementation and closest-match
  selection by class distance.
- **Post-processing** - [`ProblemPostProcessor`][ProblemPostProcessor] and
  [`PostProcessorSettings`][PostProcessorSettings], applying configured rules to the final response.
- **Output shaping** - [`ProblemFormat`][ProblemFormat], [`TypeNameMapper`][TypeNameMapper] and
  [`HttpStatusTitleResolver`][HttpStatusTitleResolver].
- **Auto-configuration** - [`ProblemAutoConfiguration`][ProblemAutoConfiguration] for generic beans,
  [`ProblemWebAutoConfiguration`][ProblemWebAutoConfiguration] for web-related beans (web applications only), and
  [`ProblemJacksonAutoConfiguration`][ProblemJacksonAutoConfiguration] and
  [`ProblemGsonAutoConfiguration`][ProblemGsonAutoConfiguration] for serialization support.

The module ships a set of built-in `ProblemResolver` implementations covering the framework and validation exceptions
that are common to both stacks - binding and constraint violations, type mismatches, unreadable or undecodable request
bodies, unsupported media types and methods, missing request values, multipart failures, `ResponseStatusException` and
Spring's own `ErrorResponse`. They resolve those exceptions without echoing `getMessage()`, so framework internals do
not leak to clients.

Stack-specific overrides - the error fallback handlers and the `404 Not Found` unification - live in the two adapter
modules instead.

## Configuration

All properties sit under the `problem4j` prefix, bound by [`ProblemProperties`][ProblemProperties]. Individual
components are conditional, so each can be replaced by declaring your own bean or switched off through properties.

See the [full documentation][github-pages] for the property reference and usage guide.

[github-pages]: https://problem4j.github.io

[AbstractProblemResolver]: src/main/java/io/github/problem4j/spring/web/resolver/AbstractProblemResolver.java

[HttpStatusTitleResolver]: src/main/java/io/github/problem4j/spring/web/HttpStatusTitleResolver.java

[PostProcessorSettings]: src/main/java/io/github/problem4j/spring/web/PostProcessorSettings.java

[ProblemAutoConfiguration]: src/main/java/io/github/problem4j/spring/web/autoconfigure/ProblemAutoConfiguration.java
[ProblemGsonAutoConfiguration]: src/main/java/io/github/problem4j/spring/web/autoconfigure/ProblemGsonAutoConfiguration.java
[ProblemJacksonAutoConfiguration]: src/main/java/io/github/problem4j/spring/web/autoconfigure/ProblemJacksonAutoConfiguration.java
[ProblemWebAutoConfiguration]: src/main/java/io/github/problem4j/spring/web/autoconfigure/ProblemWebAutoConfiguration.java

[ProblemFormat]: src/main/java/io/github/problem4j/spring/web/ProblemFormat.java

[ProblemPostProcessor]: src/main/java/io/github/problem4j/spring/web/ProblemPostProcessor.java

[ProblemProperties]: src/main/java/io/github/problem4j/spring/web/autoconfigure/ProblemProperties.java

[ProblemResolver]: src/main/java/io/github/problem4j/spring/web/resolver/ProblemResolver.java

[ProblemResolverStore]: src/main/java/io/github/problem4j/spring/web/ProblemResolverStore.java

[TypeNameMapper]: src/main/java/io/github/problem4j/spring/web/TypeNameMapper.java
