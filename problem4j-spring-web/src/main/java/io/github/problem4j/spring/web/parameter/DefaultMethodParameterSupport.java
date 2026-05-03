/*
 * Copyright 2025-2026 The Problem4J Authors
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

/**
 * Default implementation of {@link MethodParameterSupport}.
 *
 * @since 1.2.0
 */
public class DefaultMethodParameterSupport implements MethodParameterSupport {

  /**
   * Resolve a stable logical name for a method parameter, honoring supported Spring binding
   * annotations. If an annotation supplies an explicit {@code name} or {@code value}, that wins;
   * otherwise falls back to the parameter's discovered name. Unknown or unsupported annotations are
   * ignored.
   *
   * @param parameter Spring {@link org.springframework.core.MethodParameter} (may be {@code null})
   * @return optional parameter name; empty if the input is {@code null}
   * @since 1.2.0
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
   * @since 1.2.0
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
   * @since 1.2.0
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
   * @since 1.2.0
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
   * @since 1.2.0
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
   * @since 1.2.0
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
   * @since 1.2.0
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
   * @since 1.2.0
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
   * @since 1.2.0
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
