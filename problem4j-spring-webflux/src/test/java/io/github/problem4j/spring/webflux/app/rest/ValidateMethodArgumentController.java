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

package io.github.problem4j.spring.webflux.app.rest;

import io.github.problem4j.spring.webflux.app.model.QueryBindCtorsRecord;
import io.github.problem4j.spring.webflux.app.model.QueryBindObject;
import io.github.problem4j.spring.webflux.app.model.QueryBindRecord;
import io.github.problem4j.spring.webflux.app.model.QueryObject;
import io.github.problem4j.spring.webflux.app.model.QueryRecord;
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
