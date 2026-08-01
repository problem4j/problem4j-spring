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

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping(path = "/missing-parameter")
public class MissingParameterController {

  @GetMapping(path = {"/path-variable", "/path-variable/{var}"})
  public String pathVariable(@PathVariable("var") String var) {
    return "OK";
  }

  @GetMapping(path = "/request-param")
  public String requestParam(@RequestParam("param") String param) {
    return "OK";
  }

  @PostMapping(path = "/request-part")
  public String requestPart(@RequestPart("file") FilePart file) {
    return "OK";
  }

  @GetMapping(path = "/request-header")
  public String requestHeader(@RequestHeader("X-Custom-Header") String xCustomHeader) {
    return "OK";
  }

  @GetMapping(path = "/cookie-value")
  public String cookieValue(@CookieValue("x_session") String xSession) {
    return "OK";
  }

  @GetMapping(path = "/request-attribute")
  public String requestAttribute(@RequestAttribute("attr") String attr) {
    return "OK";
  }

  @GetMapping(path = "/session-attribute")
  public String sessionAttribute(@SessionAttribute("attr") String attr) {
    return "OK";
  }
}
