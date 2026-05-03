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

import io.github.problem4j.spring.webmvc.app.model.EnumRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/type-mismatch")
public class TypeMismatchController {

  @GetMapping(path = "/path-variable/{id}")
  public String pathVariable(@PathVariable("id") Integer id) {
    return "OK";
  }

  @GetMapping(path = "/request-param")
  public String requestParam(@RequestParam("id") Integer id) {
    return "OK";
  }

  @GetMapping(path = "/request-header")
  public String requestHeader(@RequestHeader("X-Id") Integer id) {
    return "OK";
  }

  @GetMapping(path = "/cookie-value")
  public String cookieValue(@CookieValue("id") Integer id) {
    return "OK";
  }

  @PostMapping(path = "/request-body")
  @ResponseStatus(HttpStatus.OK)
  public void requestBody(@RequestBody EnumRequest request) {}
}
