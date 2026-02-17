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
