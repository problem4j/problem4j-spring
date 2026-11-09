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

package io.github.problem4j.spring.webflux.app.rest;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;

import io.github.problem4j.core.ProblemContext;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/problem-context")
public class ProblemContextArgumentController {

  @GetMapping(path = "/argument")
  public String argument(@Nullable ProblemContext context) {
    return context != null ? String.valueOf(context.get("traceId")) : "no-context";
  }

  @GetMapping(path = "/optional")
  public String optional(Optional<ProblemContext> context) {
    return context.map(it -> String.valueOf(it.get("traceId"))).orElse("no-context");
  }

  @GetMapping(path = "/request-attribute")
  public String requestAttribute(
      @RequestAttribute(PROBLEM_CONTEXT_ATTRIBUTE) ProblemContext context) {
    return String.valueOf(context.get("traceId"));
  }

  @GetMapping(path = "/both")
  public String both(
      ProblemContext context,
      @RequestAttribute(PROBLEM_CONTEXT_ATTRIBUTE) ProblemContext attribute) {
    return String.valueOf(context == attribute);
  }
}
