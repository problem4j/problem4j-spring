# Problem4J Spring WebMVC

Spring WebMVC integration, for the servlet stack. It builds on[`problem4j-spring-web`](../problem4j-spring-web), which
resolves the framework and validation exceptions shared by both stacks, and adds the handling that only applies to
`spring-webmvc`.

This is the module to depend on in a WebMVC application.

## Override `404 Not Found`

- `NoHandlerFoundException`
- `NoResourceFoundException`

Both resolve to the same response, so nothing reveals whether a path was meant to be a static resource or a controller
mapping.

```json
{
  "status": 404,
  "title": "Not Found"
}
```

## Override `ProblemErrorController`

[`ProblemErrorController`][ProblemErrorController] replaces the default `spring-webmvc` error fallback, which inspects
the `Accept` header to decide between a formatted error page and JSON built from `ErrorAttributes`. The override always
produces a `Problem` instead.

- Declare your own `ErrorController` bean to override it further.
- Exclude [`ProblemErrorMvcConfiguration`][ProblemErrorMvcConfiguration] to disable the override.

[ProblemErrorController]: src/main/java/io/github/problem4j/spring/webmvc/ProblemErrorController.java

[ProblemErrorMvcConfiguration]: src/main/java/io/github/problem4j/spring/webmvc/autoconfigure/ProblemErrorMvcConfiguration.java
