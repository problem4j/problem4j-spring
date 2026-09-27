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
import io.github.problem4j.spring.web.ProblemHandler;
import io.github.problem4j.spring.webmvc.app.problem.ScopedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/scoped-handler")
public class ScopedHandlerController {

  @GetMapping
  public String scoped() {
    throw new ScopedException();
  }

  @GetMapping(path = "/missing-param")
  public String missingParam(@RequestParam("value") String value) {
    return value;
  }

  @ProblemHandler
  Problem scoped(ScopedException ex) {
    return Problem.builder().title("Scoped").status(409).build();
  }

  @ProblemHandler
  Problem missingParam(MissingServletRequestParameterException ex) {
    return Problem.builder().title("Scoped missing parameter").status(422).build();
  }
}
