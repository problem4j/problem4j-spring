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

package io.github.problem4j.spring.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.web.parameter.DefaultMethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

class DefaultMethodParameterSupportTest {

  private final MethodParameterSupport support = new DefaultMethodParameterSupport();

  @Test
  void givenPathVariable_whenFindParameterName_thenReturnsName() throws NoSuchMethodException {
    Method method = TestController.class.getMethod("pathVariableMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);

    Optional<String> name = support.findParameterName(parameter);

    assertThat(name).contains("id");
  }

  @Test
  void givenRequestParam_whenFindParameterName_thenReturnsName() throws NoSuchMethodException {
    Method method = TestController.class.getMethod("requestParamMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);

    Optional<String> name = support.findParameterName(parameter);

    assertThat(name).contains("param");
  }

  @Test
  void givenRequestHeader_whenFindParameterName_thenReturnsName() throws NoSuchMethodException {
    Method method = TestController.class.getMethod("requestHeaderMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);

    Optional<String> name = support.findParameterName(parameter);

    assertThat(name).contains("X-Custom-Header");
  }

  @Test
  void givenCookieValue_whenFindParameterName_thenReturnsName() throws NoSuchMethodException {
    Method method = TestController.class.getMethod("cookieValueMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);

    Optional<String> name = support.findParameterName(parameter);

    assertThat(name).contains("SESSIONID");
  }

  @Test
  void givenSessionAttribute_whenFindParameterName_thenReturnsName() throws NoSuchMethodException {
    Method method = TestController.class.getMethod("sessionAttributeMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);

    Optional<String> name = support.findParameterName(parameter);

    assertThat(name).contains("user");
  }

  @Test
  void givenRequestAttribute_whenFindParameterName_thenReturnsName() throws NoSuchMethodException {
    Method method = TestController.class.getMethod("requestAttributeMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);

    Optional<String> name = support.findParameterName(parameter);

    assertThat(name).contains("attr");
  }

  @Test
  void givenMatrixVariable_whenFindParameterName_thenReturnsName() throws NoSuchMethodException {
    Method method = TestController.class.getMethod("matrixVariableMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);

    Optional<String> name = support.findParameterName(parameter);

    assertThat(name).contains("matrix");
  }

  @Test
  void givenNoAnnotation_whenFindParameterName_thenReturnsName() throws NoSuchMethodException {
    Method method = TestController.class.getMethod("noAnnotationMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);
    parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());

    Optional<String> name = support.findParameterName(parameter);

    assertThat(name).contains("value");
  }

  static class TestController {

    public void pathVariableMethod(@PathVariable("id") String id) {}

    public void requestParamMethod(@RequestParam("param") String param) {}

    public void requestHeaderMethod(@RequestHeader("X-Custom-Header") String header) {}

    public void cookieValueMethod(@CookieValue("SESSIONID") String cookie) {}

    public void sessionAttributeMethod(@SessionAttribute("user") String user) {}

    public void requestAttributeMethod(@RequestAttribute("attr") String attr) {}

    public void matrixVariableMethod(@MatrixVariable("matrix") String matrix) {}

    public void noAnnotationMethod(String value) {}
  }
}
