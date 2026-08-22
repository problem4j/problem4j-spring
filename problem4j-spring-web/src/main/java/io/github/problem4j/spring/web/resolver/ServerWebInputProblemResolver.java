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
 * @since 1.2.0
 */
public class ServerWebInputProblemResolver extends AbstractProblemResolver
    implements TypeNameMapperAware, MethodParameterSupportAware, TypeMismatchProblemResolverAware {

  private TypeMismatchProblemResolver typeMismatchProblemResolver;
  private MethodParameterSupport methodParameterSupport;
  private final JacksonErrorHelper jacksonErrorHelper;

  /**
   * Creates a new {@link ServerWebInputProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public ServerWebInputProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link ServerWebInputProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
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
   */
  public ServerWebInputProblemResolver(
      ProblemFormat problemFormat, MethodParameterSupport methodParameterSupport) {
    this(
        problemFormat,
        new TypeMismatchProblemResolver(problemFormat),
        methodParameterSupport,
        new SimpleTypeNameMapper());
  }

  /**
   * Creates a new {@link ServerWebInputProblemResolver} with the specified type mismatch resolver
   * and method parameter support, and default problem format.
   *
   * @param typeMismatchProblemResolver the resolver to use
   * @param methodParameterSupport the support for extracting parameter names
   * @since 3.1.0
   */
  public ServerWebInputProblemResolver(
      TypeMismatchProblemResolver typeMismatchProblemResolver,
      MethodParameterSupport methodParameterSupport) {
    this(
        ProblemFormat.identity(),
        typeMismatchProblemResolver,
        methodParameterSupport,
        new SimpleTypeNameMapper());
  }

  /**
   * Creates a new {@link ServerWebInputProblemResolver} with the specified problem format, type
   * mismatch resolver, and method parameter support.
   *
   * @param problemFormat the problem format to use
   * @param typeMismatchProblemResolver the resolver to use
   * @param methodParameterSupport the support for extracting parameter names
   * @param typeNameMapper the type name mapper to use for decoding exceptions
   * @since 1.2.0
   */
  public ServerWebInputProblemResolver(
      ProblemFormat problemFormat,
      TypeMismatchProblemResolver typeMismatchProblemResolver,
      MethodParameterSupport methodParameterSupport,
      TypeNameMapper typeNameMapper) {
    super(ServerWebInputException.class, problemFormat);
    this.typeMismatchProblemResolver = typeMismatchProblemResolver;
    this.methodParameterSupport = methodParameterSupport;
    this.jacksonErrorHelper = new JacksonErrorHelper(problemFormat, typeNameMapper);
  }

  /**
   * Replaces the {@link ProblemFormat} used by this resolver, cascading it into the internal type
   * mismatch resolver and Jackson error helper.
   *
   * @param problemFormat the problem format to use
   * @since 3.1.0
   */
  @Override
  public void setProblemFormat(ProblemFormat problemFormat) {
    super.setProblemFormat(problemFormat);
    typeMismatchProblemResolver.setProblemFormat(problemFormat);
    jacksonErrorHelper.setProblemFormat(problemFormat);
  }

  /**
   * Replaces the {@link TypeNameMapper} used by this resolver, cascading it into the internal type
   * mismatch resolver and Jackson error helper.
   *
   * @param typeNameMapper the type name mapper to use
   * @since 3.1.0
   */
  @Override
  public void setTypeNameMapper(TypeNameMapper typeNameMapper) {
    typeMismatchProblemResolver.setTypeNameMapper(typeNameMapper);
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
   * Replaces the {@link TypeMismatchProblemResolver} used by this resolver.
   *
   * @param typeMismatchProblemResolver the resolver to use
   * @since 3.1.0
   */
  @Override
  public void setTypeMismatchProblemResolver(
      TypeMismatchProblemResolver typeMismatchProblemResolver) {
    this.typeMismatchProblemResolver = typeMismatchProblemResolver;
  }

  /**
   * Resolves a {@link ServerWebInputException} into an immutable {@link Problem}. If the root cause
   * is a {@link TypeMismatchException}, delegates to {@link TypeMismatchProblemResolver} and, when
   * missing, attempts to append the offending property/parameter name as the {@code
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
      return resolveTypeMismatchException(context, headers, status, swie, tme);
    } else if (swie.getCause() instanceof DecodingException de) {
      return resolveDecodingException(de);
    }

    return Problem.of(swie.getStatusCode().value());
  }

  private Problem resolveTypeMismatchException(
      ProblemContext context,
      HttpHeaders headers,
      HttpStatusCode status,
      ServerWebInputException swie,
      TypeMismatchException tme) {
    Problem problem = typeMismatchProblemResolver.resolve(context, tme, headers, status);
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
