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

import io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice;
import io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter;
import io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

class ProblemWebMvcAutoConfigurationTest {

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithEnabled {

    @Autowired(required = false)
    private ProblemWebMvcAutoConfiguration problemWebMvcAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired(required = false)
    private ProblemResolverWebMvcConfiguration problemResolverWebMvcConfiguration;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemWebMvcAutoConfiguration).isNotNull();
      assertThat(problemErrorMvcConfiguration).isNotNull();
      assertThat(problemResolverWebMvcConfiguration).isNotNull();

      assertThat(properties.isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.enabled=false"})
  @Nested
  class WithDisabled {

    @Autowired(required = false)
    private ProblemWebMvcAutoConfiguration problemWebMvcAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired(required = false)
    private ProblemResolverWebMvcConfiguration problemResolverWebMvcConfiguration;

    @Autowired(required = false)
    private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutConfiguration() {
      assertThat(problemWebMvcAutoConfiguration).isNull();
      assertThat(problemErrorMvcConfiguration).isNull();
      assertThat(problemResolverWebMvcConfiguration).isNull();

      assertThat(properties).isNull();
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithExceptionAdviceEnabled {

    @Autowired(required = false)
    private ExceptionWebMvcAdvice exceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionWebMvcAdvice).isNotNull();
      assertThat(properties.getExceptionAdvice().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.exception-advice.enabled=false"})
  @Nested
  class WithExceptionAdviceDisabled {

    @Autowired(required = false)
    private ExceptionWebMvcAdvice exceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionWebMvcAdvice).isNull();
      assertThat(properties.getExceptionAdvice().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithProblemExceptionAdviceEnabled {

    @Autowired(required = false)
    private ProblemExceptionWebMvcAdvice problemExceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemExceptionWebMvcAdvice).isNotNull();
      assertThat(properties.getProblemExceptionAdvice().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.problem-exception-advice.enabled=false"})
  @Nested
  class WithProblemExceptionAdviceDisabled {

    @Autowired(required = false)
    private ProblemExceptionWebMvcAdvice problemExceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemExceptionWebMvcAdvice).isNull();
      assertThat(properties.getProblemExceptionAdvice().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithProblemContextFilterEnabled {

    @Autowired(required = false)
    private ProblemContextWebMvcFilter problemContextWebMvcFilter;

    @Autowired(required = false)
    private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemContextWebMvcFilter).isNotNull();
      assertThat(properties.getProblemContextFilter().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.problem-context-filter.enabled=false"})
  @Nested
  class WithProblemContextFilterDisabled {

    @Autowired(required = false)
    private ProblemContextWebMvcFilter problemContextWebMvcFilter;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemContextWebMvcFilter).isNull();
      assertThat(properties.getProblemContextFilter().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithExceptionHandlerEnabled {

    @Autowired(required = false)
    private ResponseEntityExceptionHandler exceptionHandler;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionHandler).isNotNull();
      assertThat(properties.getExceptionHandler().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.exception-handler.enabled=false"})
  @Nested
  class WithExceptionHandlerDisabled {

    @Autowired(required = false)
    private ResponseEntityExceptionHandler exceptionHandler;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionHandler).isNull();
      assertThat(properties.getExceptionHandler().isEnabled()).isFalse();
    }
  }
}
