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

package io.github.problem4j.spring.webflux.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.webflux.ExceptionWebFluxAdvice;
import io.github.problem4j.spring.webflux.ProblemContextWebFluxFilter;
import io.github.problem4j.spring.webflux.ProblemExceptionWebFluxAdvice;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

class ProblemWebFluxAutoConfigurationTest {

  @SpringBootTest(classes = {WebFluxTestApp.class})
  @Nested
  class WithEnabled {

    @Autowired(required = false)
    private ProblemWebFluxAutoConfiguration problemWebFluxAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorWebFluxConfiguration problemErrorWebFluxConfiguration;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemWebFluxAutoConfiguration).isNotNull();
      assertThat(problemErrorWebFluxConfiguration).isNotNull();

      assertThat(properties.isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      properties = {"problem4j.webflux.enabled=false"})
  @Nested
  class WithDisabled {

    @Autowired(required = false)
    private ProblemWebFluxAutoConfiguration problemWebFluxAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorWebFluxConfiguration problemErrorWebFluxConfiguration;

    @Autowired(required = false)
    private ProblemWebFluxProperties properties;

    @Test
    void contextLoadsWithoutProblemConfiguration() {
      assertThat(problemWebFluxAutoConfiguration).isNull();
      assertThat(problemErrorWebFluxConfiguration).isNull();

      assertThat(properties).isNull();
    }
  }

  @SpringBootTest(classes = {WebFluxTestApp.class})
  @Nested
  class WithExceptionAdviceEnabled {

    @Autowired(required = false)
    private ExceptionWebFluxAdvice exceptionWebFluxAdvice;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionWebFluxAdvice).isNotNull();
      assertThat(properties.getExceptionAdvice().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      properties = {"problem4j.webflux.exception-advice.enabled=false"})
  @Nested
  class WithExceptionAdviceDisabled {

    @Autowired(required = false)
    private ExceptionWebFluxAdvice exceptionWebFluxAdvice;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionWebFluxAdvice).isNull();
      assertThat(properties.getExceptionAdvice().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebFluxTestApp.class})
  @Nested
  class WithProblemExceptionAdviceEnabled {

    @Autowired(required = false)
    private ProblemExceptionWebFluxAdvice problemExceptionWebFluxAdvice;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemExceptionWebFluxAdvice).isNotNull();
      assertThat(properties.getProblemExceptionAdvice().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      properties = {"problem4j.webflux.problem-exception-advice.enabled=false"})
  @Nested
  class WithProblemExceptionAdviceDisabled {

    @Autowired(required = false)
    private ProblemExceptionWebFluxAdvice problemExceptionWebFluxAdvice;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemExceptionWebFluxAdvice).isNull();
      assertThat(properties.getProblemExceptionAdvice().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebFluxTestApp.class})
  @Nested
  class WithProblemContextFilterEnabled {

    @Autowired(required = false)
    private ProblemContextWebFluxFilter problemContextWebFluxFilter;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemContextWebFluxFilter).isNotNull();
      assertThat(properties.getProblemContextFilter().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      properties = {"problem4j.webflux.problem-context-filter.enabled=false"})
  @Nested
  class WithProblemContextFilterDisabled {

    @Autowired(required = false)
    private ProblemContextWebFluxFilter problemContextWebFluxFilter;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemContextWebFluxFilter).isNull();
      assertThat(properties.getProblemContextFilter().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebFluxTestApp.class})
  @Nested
  class WithExceptionHandlerEnabled {

    @Autowired(required = false)
    private ResponseEntityExceptionHandler exceptionHandler;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionHandler).isNotNull();
      assertThat(properties.getExceptionHandler().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebFluxTestApp.class},
      properties = {"problem4j.webflux.exception-handler.enabled=false"})
  @Nested
  class WithExceptionHandlerDisabled {

    @Autowired(required = false)
    private ResponseEntityExceptionHandler exceptionHandler;

    @Autowired private ProblemWebFluxProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionHandler).isNull();
      assertThat(properties.getExceptionHandler().isEnabled()).isFalse();
    }
  }
}
