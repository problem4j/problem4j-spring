/*
 * Copyright (c) 2025-2026 The Problem4J Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.ProblemSupport.PROPERTY_EXTENSION;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.SimpleTypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.parameter.DefaultMethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import java.util.Optional;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebInputException;

/**
 * Handles {@link ServerWebInputException} thrown when request data cannot be properly read or
 * converted in a WebFlux application.
 *
 * <p>This typically occurs for invalid query parameters, path variables, or request body content
 * that cannot be converted to the target method parameter type.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 400 (Bad Request) response to
 * indicate that the client sent invalid or unreadable input.
 */
public class ServerWebInputProblemResolver extends AbstractProblemResolver {

  private final TypeMismatchProblemResolver typeMismatchProblemResolver;
  private final MethodParameterSupport methodParameterSupport;
  private final JacksonErrorHelper jacksonErrorHelper;

  /** Creates a new {@link ServerWebInputProblemResolver} with default problem format. */
  public ServerWebInputProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link ServerWebInputProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
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
   * Creates a new {@link ServerWebInputProblemResolver} with the specified problem format, type
   * mismatch resolver, and method parameter support.
   *
   * @param problemFormat the problem format to use
   * @param typeMismatchProblemResolver the resolver to use
   * @param methodParameterSupport the support for extracting parameter names
   * @param typeNameMapper the type name mapper to use for decoding exceptions
   */
  public ServerWebInputProblemResolver(
      ProblemFormat problemFormat,
      TypeMismatchProblemResolver typeMismatchProblemResolver,
      MethodParameterSupport methodParameterSupport,
      TypeNameMapper typeNameMapper) {
    super(ServerWebInputException.class, problemFormat);
    this.typeMismatchProblemResolver = typeMismatchProblemResolver;
    this.methodParameterSupport = methodParameterSupport;
    jacksonErrorHelper = new JacksonErrorHelper(problemFormat, typeNameMapper);
  }

  /**
   * Resolves a {@link ServerWebInputException} into a {@link ProblemBuilder}. If the root cause is
   * a {@link TypeMismatchException}, delegates to {@link TypeMismatchProblemResolver} and, when
   * missing, attempts to append the offending property/parameter name as the {@code
   * ProblemSupport#PROPERTY_EXTENSION}. Otherwise, returns a builder whose status reflects the
   * exception's embedded HTTP status code.
   *
   * @param context problem context (unused for this resolver)
   * @param ex the triggering {@link ServerWebInputException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; status derived from exception)
   * @return builder representing the invalid input condition
   * @see io.github.problem4j.spring.web.ProblemSupport#PROPERTY_EXTENSION
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ServerWebInputException swie = (ServerWebInputException) ex;

    if (swie.getCause() instanceof TypeMismatchException tme) {
      return resolveTypeMismatchException(context, headers, status, swie, tme);
    } else if (swie.getCause() instanceof DecodingException de) {
      return resolveDecodingException(de);
    }

    return Problem.builder().status(swie.getStatusCode().value());
  }

  private ProblemBuilder resolveTypeMismatchException(
      ProblemContext context,
      HttpHeaders headers,
      HttpStatusCode status,
      ServerWebInputException swie,
      TypeMismatchException tme) {
    ProblemBuilder builder =
        typeMismatchProblemResolver.resolveBuilder(context, tme, headers, status);
    if (!builder.build().hasExtension(PROPERTY_EXTENSION)) {
      return tryAppendingPropertyFromMethodParameter(swie.getMethodParameter(), builder);
    }
    return builder;
  }

  private ProblemBuilder tryAppendingPropertyFromMethodParameter(
      MethodParameter parameter, ProblemBuilder builder) {
    Optional<String> optionalProperty = methodParameterSupport.findParameterName(parameter);
    if (optionalProperty.isPresent()) {
      builder = builder.extension(PROPERTY_EXTENSION, optionalProperty.get());
    }
    return builder;
  }

  private ProblemBuilder resolveDecodingException(DecodingException ex) {
    if (ex.getCause() instanceof MismatchedInputException e) {
      return jacksonErrorHelper.resolveMismatchedInput(e);
    }
    return Problem.builder().status(HttpStatus.BAD_REQUEST.value());
  }
}
