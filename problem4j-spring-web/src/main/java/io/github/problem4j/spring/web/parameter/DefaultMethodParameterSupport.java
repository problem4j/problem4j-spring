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

package io.github.problem4j.spring.web.parameter;

import java.lang.annotation.Annotation;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttribute;

/** Default implementation of {@link MethodParameterSupport}. */
public class DefaultMethodParameterSupport implements MethodParameterSupport {

  /**
   * Resolve a stable logical name for a method parameter, honoring supported Spring binding
   * annotations. If an annotation supplies an explicit {@code name} or {@code value}, that wins;
   * otherwise falls back to the parameter's discovered name. Unknown or unsupported annotations are
   * ignored.
   *
   * @param parameter Spring {@link org.springframework.core.MethodParameter} (may be {@code null})
   * @return optional parameter name; empty if the input is {@code null}
   */
  @Override
  public Optional<String> findParameterName(@Nullable MethodParameter parameter) {
    if (parameter == null) {
      return Optional.empty();
    }

    Annotation[] annotations = parameter.getParameterAnnotations();
    String fieldName = parameter.getParameterName();
    for (Annotation annotation : annotations) {
      if (annotation instanceof PathVariable pathVariable) {
        fieldName = findPathVariableName(pathVariable, fieldName);
        return Optional.ofNullable(fieldName);
      } else if (annotation instanceof RequestParam requestParam) {
        fieldName = findRequestParamName(requestParam, fieldName);
        return Optional.ofNullable(fieldName);
      } else if (annotation instanceof RequestPart requestPart) {
        fieldName = findRequestPartName(requestPart, fieldName);
        return Optional.ofNullable(fieldName);
      } else if (annotation instanceof RequestHeader requestHeader) {
        fieldName = findRequestHeaderName(requestHeader, fieldName);
        return Optional.ofNullable(fieldName);
      } else if (annotation instanceof CookieValue cookieValue) {
        fieldName = findCookieValueName(cookieValue, fieldName);
        return Optional.ofNullable(fieldName);
      } else if (annotation instanceof SessionAttribute sessionAttribute) {
        fieldName = findSessionAttributeName(sessionAttribute, fieldName);
        return Optional.ofNullable(fieldName);
      } else if (annotation instanceof RequestAttribute requestAttribute) {
        fieldName = findRequestAttributeName(requestAttribute, fieldName);
        return Optional.ofNullable(fieldName);
      } else if (annotation instanceof MatrixVariable matrixVariable) {
        fieldName = findMatrixVariableName(matrixVariable, fieldName);
        return Optional.ofNullable(fieldName);
      }
    }
    return Optional.ofNullable(fieldName);
  }

  /**
   * Derive the effective name for a {@link PathVariable}, preferring {@code name} then {@code
   * value}.
   *
   * @param annotation path variable annotation
   * @param defaultName fallback (parameter name)
   * @return resolved name or fallback
   */
  protected @Nullable String findPathVariableName(
      PathVariable annotation, @Nullable String defaultName) {
    String name = annotation.name();
    if (!StringUtils.hasLength(name)) {
      name = annotation.value();
    }
    return StringUtils.hasLength(name) ? name : defaultName;
  }

  /**
   * Derive the effective name for a {@link RequestParam}, preferring {@code name} then {@code
   * value}.
   *
   * @param annotation request param annotation
   * @param defaultName fallback (parameter name)
   * @return resolved name or fallback
   */
  protected @Nullable String findRequestParamName(
      RequestParam annotation, @Nullable String defaultName) {
    String name = annotation.name();
    if (!StringUtils.hasLength(name)) {
      name = annotation.value();
    }
    return StringUtils.hasLength(name) ? name : defaultName;
  }

  /**
   * Derive the effective name for a {@link RequestPart}, preferring {@code name} then {@code
   * value}.
   *
   * @param annotation request part annotation
   * @param defaultName fallback (parameter name)
   * @return resolved name or fallback
   */
  protected @Nullable String findRequestPartName(
      RequestPart annotation, @Nullable String defaultName) {
    String name = annotation.name();
    if (!StringUtils.hasLength(name)) {
      name = annotation.value();
    }
    return StringUtils.hasLength(name) ? name : defaultName;
  }

  /**
   * Derive the effective name for a {@link RequestHeader}, preferring {@code name} then {@code
   * value}.
   *
   * @param annotation request header annotation
   * @param defaultName fallback (parameter name)
   * @return resolved name or fallback
   */
  protected @Nullable String findRequestHeaderName(
      RequestHeader annotation, @Nullable String defaultName) {
    String name = annotation.name();
    if (!StringUtils.hasLength(name)) {
      name = annotation.value();
    }
    return StringUtils.hasLength(name) ? name : defaultName;
  }

  /**
   * Derive the effective name for a {@link CookieValue}, preferring {@code name} then {@code
   * value}.
   *
   * @param annotation cookie value annotation
   * @param defaultName fallback (parameter name)
   * @return resolved name or fallback
   */
  protected @Nullable String findCookieValueName(
      CookieValue annotation, @Nullable String defaultName) {
    String name = annotation.name();
    if (!StringUtils.hasLength(name)) {
      name = annotation.value();
    }
    return StringUtils.hasLength(name) ? name : defaultName;
  }

  /**
   * Derive the effective name for a {@link SessionAttribute}, preferring {@code name} then {@code
   * value}.
   *
   * @param annotation session attribute annotation
   * @param defaultName fallback (parameter name)
   * @return resolved name or fallback
   */
  protected @Nullable String findSessionAttributeName(
      SessionAttribute annotation, @Nullable String defaultName) {
    String name = annotation.name();
    if (!StringUtils.hasLength(name)) {
      name = annotation.value();
    }
    return StringUtils.hasLength(name) ? name : defaultName;
  }

  /**
   * Derive the effective name for a {@link RequestAttribute}, preferring {@code name} then {@code
   * value}.
   *
   * @param annotation request attribute annotation
   * @param defaultName fallback (parameter name)
   * @return resolved name or fallback
   */
  protected @Nullable String findRequestAttributeName(
      RequestAttribute annotation, @Nullable String defaultName) {
    String name = annotation.name();
    if (!StringUtils.hasLength(name)) {
      name = annotation.value();
    }
    return StringUtils.hasLength(name) ? name : defaultName;
  }

  /**
   * Derive the effective name for a {@link MatrixVariable}, preferring {@code name} then {@code
   * value}.
   *
   * @param annotation matrix variable annotation
   * @param defaultName fallback (parameter name)
   * @return resolved name or fallback
   */
  protected @Nullable String findMatrixVariableName(
      MatrixVariable annotation, @Nullable String defaultName) {
    String name = annotation.name();
    if (!StringUtils.hasLength(name)) {
      name = annotation.value();
    }
    return StringUtils.hasLength(name) ? name : defaultName;
  }
}
