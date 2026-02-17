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

package io.github.problem4j.spring.webmvc.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.webmvc.ProblemErrorController;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.error.ErrorController;

class ProblemErrorMvcConfigurationTest {

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithEnabled {

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired private ErrorController errorController;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemErrorMvcConfiguration).isNotNull();
      assertThat(errorController).isInstanceOf(ProblemErrorController.class);
      assertThat(properties.getErrorController().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.error-controller.enabled=false"})
  @Nested
  class WithDisabled {

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired private ErrorController errorController;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemErrorMvcConfiguration).isNull();
      assertThat(errorController).isNotInstanceOf(ProblemErrorController.class);
      assertThat(properties.getErrorController().isEnabled()).isFalse();
    }
  }
}
