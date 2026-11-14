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

package io.github.problem4j.spring.webmvc.app.rest;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.problem.AdviceOverriddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHookAdvice {

  @ExceptionHandler(AdviceOverriddenException.class)
  public ResponseEntity<Problem> handle(AdviceOverriddenException ex) {
    return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
        .body(Problem.builder().title("I'm a teapot").status(418).detail("app advice").build());
  }
}
