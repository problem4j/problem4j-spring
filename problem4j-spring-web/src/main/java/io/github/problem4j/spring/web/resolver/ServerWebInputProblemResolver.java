/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.parameter.ViolationSupport.PROPERTY_EXTENSION;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.SimpleTypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapperAware;
import io.github.problem4j.spring.web.config.ProblemBeanPostProcessor;
import io.github.problem4j.spring.web.parameter.DefaultMethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupportAware;
import java.util.Optional;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebInputException;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Handles {@link ServerWebInputException} thrown when request data cannot be properly read or
 * converted in a WebFlux application.
 *
 * <p>This typically occurs for invalid query parameters, path variables, or request body content
 * that cannot be converted to the target method parameter type.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 400 (Bad Request) response to
 * indicate that the client sent invalid or unreadable input.
 *
 * <p>When used as a Spring bean, in addition to the {@link ProblemFormat} injected via {@link
 * AbstractProblemResolver}, the {@link TypeNameMapper} and {@link MethodParameterSupport} are
 * assigned after construction by {@link ProblemBeanPostProcessor} through {@link
 * #setTypeNameMapper(TypeNameMapper)} and {@link
 * #setMethodParameterSupport(MethodParameterSupport)}.
 *
 * @since 1.2.0
 */
public class ServerWebInputProblemResolver extends AbstractProblemResolver
    implements TypeNameMapperAware, MethodParameterSupportAware {

  private final TypeMismatchHelper typeMismatchHelper;
  private final JacksonErrorHelper jacksonErrorHelper;

  private MethodParameterSupport methodParameterSupport;

  /**
   * Creates a new {@link ServerWebInputProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public ServerWebInputProblemResolver() {
    super(ServerWebInputException.class);
    TypeNameMapper typeNameMapper = new SimpleTypeNameMapper();
    this.methodParameterSupport = new DefaultMethodParameterSupport();
    this.typeMismatchHelper = new TypeMismatchHelper(ProblemFormat.identity(), typeNameMapper);
    this.jacksonErrorHelper = new JacksonErrorHelper(ProblemFormat.identity(), typeNameMapper);
  }

  /**
   * Creates a new {@link ServerWebInputProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link ProblemBeanPostProcessor} now assigns collaborators after
   *     construction; use {@link #ServerWebInputProblemResolver()}
   */
  @Deprecated(since = "3.1.0", forRemoval = true)
  public ServerWebInputProblemResolver(ProblemFormat problemFormat) {
    this(problemFormat, new DefaultMethodParameterSupport());
  }

  /**
   * Creates a new {@link ServerWebInputProblemResolver} with the specified problem format and
   * method parameter support.
   *
   * @param problemFormat the problem format to use
   * @param methodParameterSupport the support for extracting parameter names
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link ProblemBeanPostProcessor} now assigns collaborators after
   *     construction; use {@link #ServerWebInputProblemResolver()}
   */
  @Deprecated(since = "3.1.0", forRemoval = true)
  public ServerWebInputProblemResolver(
      ProblemFormat problemFormat, MethodParameterSupport methodParameterSupport) {
    this(problemFormat, new SimpleTypeNameMapper(), methodParameterSupport);
  }

  @SuppressWarnings("removal")
  private ServerWebInputProblemResolver(
      ProblemFormat problemFormat,
      TypeNameMapper typeNameMapper,
      MethodParameterSupport methodParameterSupport) {
    super(ServerWebInputException.class, problemFormat);
    this.methodParameterSupport = methodParameterSupport;
    this.typeMismatchHelper = new TypeMismatchHelper(problemFormat, typeNameMapper);
    this.jacksonErrorHelper = new JacksonErrorHelper(problemFormat, typeNameMapper);
  }

  /**
   * Creates a new {@link ServerWebInputProblemResolver} with the specified problem format, type
   * mismatch resolver, and method parameter support.
   *
   * @param problemFormat the problem format to use
   * @param typeMismatchProblemResolver the resolver to use, ignored since 3.1.0
   * @param methodParameterSupport the support for extracting parameter names
   * @param typeNameMapper the type name mapper to use for decoding exceptions
   * @since 1.2.0
   * @deprecated since 3.1.0 as other constructors and {@link ProblemBeanPostProcessor
   *     ProblemBeanPostProcessor} should be used to apply components of this class
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public ServerWebInputProblemResolver(
      ProblemFormat problemFormat,
      TypeMismatchProblemResolver typeMismatchProblemResolver,
      MethodParameterSupport methodParameterSupport,
      TypeNameMapper typeNameMapper) {
    super(ServerWebInputException.class, problemFormat);
    this.methodParameterSupport = methodParameterSupport;
    this.typeMismatchHelper = new TypeMismatchHelper(problemFormat, typeNameMapper);
    this.jacksonErrorHelper = new JacksonErrorHelper(problemFormat, typeNameMapper);
  }

  /**
   * Replaces the {@link ProblemFormat} used by this resolver.
   *
   * @param problemFormat the problem format to use
   * @since 3.1.0
   */
  @Override
  public void setProblemFormat(ProblemFormat problemFormat) {
    super.setProblemFormat(problemFormat);
    typeMismatchHelper.setProblemFormat(problemFormat);
    jacksonErrorHelper.setProblemFormat(problemFormat);
  }

  /**
   * Replaces the {@link TypeNameMapper} used by this resolver.
   *
   * @param typeNameMapper the type name mapper to use
   * @since 3.1.0
   */
  @Override
  public void setTypeNameMapper(TypeNameMapper typeNameMapper) {
    typeMismatchHelper.setTypeNameMapper(typeNameMapper);
    jacksonErrorHelper.setTypeNameMapper(typeNameMapper);
  }

  /**
   * Replaces the {@link MethodParameterSupport} used by this resolver.
   *
   * @param methodParameterSupport the support for extracting parameter names
   * @since 3.1.0
   */
  @Override
  public void setMethodParameterSupport(MethodParameterSupport methodParameterSupport) {
    this.methodParameterSupport = methodParameterSupport;
  }

  /**
   * Resolves a {@link ServerWebInputException} into an immutable {@link Problem}. If the root cause
   * is a {@link TypeMismatchException}, delegates to the internal type-mismatch helper and, when
   * the property is missing, attempts to append the offending property/parameter name as the {@code
   * ViolationSupport#PROPERTY_EXTENSION}. Otherwise, returns a problem whose status reflects the
   * exception's embedded HTTP status code.
   *
   * @param context problem context (unused for this resolver)
   * @param ex the triggering {@link ServerWebInputException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; status derived from exception)
   * @return problem representing the invalid input condition
   * @see io.github.problem4j.spring.web.parameter.ViolationSupport#PROPERTY_EXTENSION
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ServerWebInputException swie = (ServerWebInputException) ex;

    if (swie.getCause() instanceof TypeMismatchException tme) {
      return resolveTypeMismatchException(swie, tme);
    } else if (swie.getCause() instanceof DecodingException de) {
      return resolveDecodingException(de);
    }

    return Problem.of(swie.getStatusCode().value());
  }

  private Problem resolveTypeMismatchException(
      ServerWebInputException swie, TypeMismatchException tme) {
    Problem problem = typeMismatchHelper.toProblem(tme);
    if (!problem.getExtensions().containsKey(PROPERTY_EXTENSION)) {
      Optional<String> optionalProperty =
          methodParameterSupport.findParameterName(swie.getMethodParameter());
      if (optionalProperty.isPresent()) {
        return problem.toBuilder().extension(PROPERTY_EXTENSION, optionalProperty.get()).build();
      }
    }
    return problem;
  }

  private Problem resolveDecodingException(DecodingException ex) {
    if (ex.getCause() instanceof MismatchedInputException e) {
      return jacksonErrorHelper.resolveMismatchedInput(e);
    }
    return Problem.of(HttpStatus.BAD_REQUEST.value());
  }
}
