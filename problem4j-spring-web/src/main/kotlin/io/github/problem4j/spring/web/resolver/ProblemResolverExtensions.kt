package io.github.problem4j.spring.web.resolver

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import kotlin.reflect.KClass
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode

/**
 * Kotlin-friendly base class for [ProblemResolver], accepting a [KClass] instead of a [Class].
 *
 * @param type exception subtype this resolver is responsible for
 * @since 3.1.0
 */
public abstract class KotlinProblemResolver(type: KClass<out Exception>) :
    AbstractProblemResolver(type.java)

/**
 * Creates a [ProblemResolver] for exception type [E] from a lambda, without subclassing
 * [AbstractProblemResolver].
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.problem
 * import io.github.problem4j.spring.web.resolver.ProblemResolver
 * import io.github.problem4j.spring.web.resolver.problemResolver
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun illegalStateProblemResolver(): ProblemResolver =
 *     problemResolver<IllegalStateException> { _, ex, _, status ->
 *       problem(500) { detail(ex.message) }
 *     }
 * ```
 *
 * @param resolve resolves the exception into a [Problem]
 * @return a [ProblemResolver] handling exceptions of type [E]
 * @since 3.1.0
 */
public inline fun <reified E : Exception> problemResolver(
    crossinline resolve: (ProblemContext, E, HttpHeaders, HttpStatusCode) -> Problem,
): ProblemResolver =
    object : AbstractProblemResolver(E::class.java) {
      override fun resolve(
          context: ProblemContext,
          ex: Exception,
          headers: HttpHeaders,
          status: HttpStatusCode,
      ) = resolve(context, ex as E, headers, status)
    }
