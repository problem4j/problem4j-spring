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
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.webmvc.app.problem.AdviceOverriddenException;
import io.github.problem4j.spring.webmvc.app.problem.ResolvableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/exception-hook")
public class ExceptionHookController {

  @GetMapping(path = "/resolvable")
  public String resolvable() {
    throw new ResolvableException();
  }

  @GetMapping(path = "/advice-overridden")
  public String adviceOverridden() {
    throw new AdviceOverriddenException();
  }

  @GetMapping(path = "/problem-server-error")
  public String problemServerError() {
    throw new ProblemException(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @GetMapping(path = "/unknown")
  public String unknown() {
    throw new IllegalStateException("unknown");
  }
}
