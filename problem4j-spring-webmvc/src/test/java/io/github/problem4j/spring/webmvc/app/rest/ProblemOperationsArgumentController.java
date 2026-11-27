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
import io.github.problem4j.spring.web.support.ProblemOperations;
import io.github.problem4j.spring.web.support.ProblemTemplate;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/problem-operations")
public class ProblemOperationsArgumentController {

  @GetMapping(path = "/argument")
  public String argument(ProblemOperations problemOperations) {
    return problemOperations.getClass().getSimpleName();
  }

  @GetMapping(path = "/template")
  public String template(ProblemTemplate problemTemplate) {
    return problemTemplate.getClass().getSimpleName();
  }

  @GetMapping(path = "/post-process")
  public String postProcess(ProblemOperations problemOperations) {
    Problem problem = problemOperations.postProcess(null, Problem.of(418));
    return String.valueOf(problem.getStatus());
  }

  @GetMapping(path = "/optional")
  public String optional(Optional<ProblemOperations> problemOperations) {
    return problemOperations.map(it -> it.getClass().getSimpleName()).orElse("none");
  }
}
