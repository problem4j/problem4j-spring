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

package io.github.problem4j.spring.webmvc.app.rest;

import io.github.problem4j.spring.webmvc.app.model.QueryBindCtorsRecord;
import io.github.problem4j.spring.webmvc.app.model.QueryBindObject;
import io.github.problem4j.spring.webmvc.app.model.QueryBindRecord;
import io.github.problem4j.spring.webmvc.app.model.QueryObject;
import io.github.problem4j.spring.webmvc.app.model.QueryRecord;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(path = "/validate-parameter")
public class ValidateMethodArgumentController {

  @GetMapping(path = "/path-variable/{id}")
  public String validatePathVariable(@PathVariable("id") @Size(min = 5) String idVar) {
    return "OK";
  }

  @GetMapping(path = "/request-param")
  public String validateRequestParam(@RequestParam("query") @Size(min = 5) String queryParam) {
    return "OK";
  }

  @GetMapping(path = "/request-header")
  public String validateRequestHeader(
      @RequestHeader("X-Custom-Header") @Size(min = 5) String xCustomHeader) {
    return "OK";
  }

  @GetMapping(path = "/cookie-value")
  public String validateCookieValue(@CookieValue("x_session") @Size(min = 5) String xSession) {
    return "OK";
  }

  @GetMapping(path = "/multi-constraint")
  public String validateMultiConstraint(
      @RequestParam("input") @Size(min = 5) @Pattern(regexp = "i") String inputParam) {
    return "OK";
  }

  @GetMapping(path = "/two-arg")
  public String validateTwoArguments(
      @RequestParam("first") @Size(min = 5) String firstParam,
      @RequestParam("second") String secondParam) {
    return "OK";
  }

  @GetMapping(path = "/three-arg")
  public String validateThreeArguments(
      @RequestParam("first") String firstParam,
      @RequestParam("second") @Size(min = 5) String secondParam,
      @RequestParam("third") String thirdParam) {
    return "OK";
  }

  @GetMapping(path = "/query-object/annotated")
  public String queryObjectAnnotated(@ModelAttribute @Valid QueryObject query) {
    return "OK";
  }

  @GetMapping(path = "/query-object/unannotated")
  public String queryObjectUnannotated(@Valid QueryObject query) {
    return "OK";
  }

  @GetMapping(path = "/query-bind-object/annotated")
  public String queryBindObjectAnnotated(@ModelAttribute @Valid QueryBindObject query) {
    return "OK";
  }

  @GetMapping(path = "/query-bind-object/unannotated")
  public String queryBindObjectUnannotated(@Valid QueryBindObject query) {
    return "OK";
  }

  @GetMapping(path = "/query-record/annotated")
  public String queryRecordAnnotated(@ModelAttribute @Valid QueryRecord query) {
    return "OK";
  }

  @GetMapping(path = "/query-record/unannotated")
  public String queryRecordUnannotated(@Valid QueryRecord query) {
    return "OK";
  }

  @GetMapping(path = "/query-bind-record/annotated")
  public String queryBindRecordAnnotated(@ModelAttribute @Valid QueryBindRecord query) {
    return "OK";
  }

  @GetMapping(path = "/query-bind-record/unannotated")
  public String queryBindRecordUnannotated(@Valid QueryBindRecord query) {
    return "OK";
  }

  // No methods for Object-based binding with multiple ctors as it's not supported by Spring. It
  // works only for records, and it will use record's canonical ctor.

  @GetMapping(path = "/query-bind-ctors-record/annotated")
  public String queryBindCtorsRecordAnnotated(@ModelAttribute @Valid QueryBindCtorsRecord query) {
    return "OK";
  }

  @GetMapping(path = "/query-bind-ctors-record/unannotated")
  public String queryBindCtorsRecordUnannotated(@Valid QueryBindCtorsRecord query) {
    return "OK";
  }
}
