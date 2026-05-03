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

package io.github.problem4j.spring.web;

import io.github.problem4j.core.Problem;
import org.springframework.http.HttpStatus;

/**
 * Small utilities used when constructing RFC 7807 {@link Problem} responses. Contains status
 * resolution helpers bridging {@link Problem} and Spring HTTP status abstractions.
 *
 * @since 3.0.0
 */
public final class ResponseSupport {

  /**
   * Resolves a {@link Problem} to a corresponding {@link HttpStatus}.
   *
   * <p>If the problem's status value is not a valid HTTP status code, {@link
   * HttpStatus#INTERNAL_SERVER_ERROR} is returned as a fallback.
   *
   * @param problem the {@link Problem} instance containing the status code
   * @return the corresponding {@link HttpStatus}, or {@link HttpStatus#INTERNAL_SERVER_ERROR} if
   *     the status is invalid
   * @see HttpStatus#INTERNAL_SERVER_ERROR
   * @since 3.0.0
   */
  public static HttpStatus resolveStatus(Problem problem) {
    try {
      return HttpStatus.valueOf(problem.getStatus());
    } catch (IllegalArgumentException e) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  private ResponseSupport() {}
}
