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

package io.github.problem4j.spring.web.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.web.app.TestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {TestApp.class})
class ProblemAutoConfigurationTest {

  @SpringBootTest(classes = {TestApp.class})
  @Nested
  class WithEnabled {

    @Autowired(required = false)
    private ProblemAutoConfiguration problemAutoConfiguration;

    @Autowired(required = false)
    private ProblemParameterConfiguration problemParameterConfiguration;

    @Autowired(required = false)
    private ProblemResolverConfiguration problemResolverConfiguration;

    @Autowired(required = false)
    private ProblemProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemAutoConfiguration).isNotNull();
      assertThat(problemParameterConfiguration).isNotNull();
      assertThat(problemResolverConfiguration).isNotNull();

      assertThat(properties).isNotNull();
      assertThat(properties.isEnabled()).isTrue();
      assertThat(properties.getResolverCaching().getMaxCacheSize())
          .isEqualTo(ProblemProperties.ResolverCaching.DEFAULT_MAX_CACHE_SIZE);
    }
  }

  @SpringBootTest(
      classes = {TestApp.class},
      properties = {"problem4j.enabled=false"})
  @Nested
  class WithDisabled {

    @Autowired(required = false)
    private ProblemAutoConfiguration problemAutoConfiguration;

    @Autowired(required = false)
    private ProblemParameterConfiguration problemParameterConfiguration;

    @Autowired(required = false)
    private ProblemResolverConfiguration problemResolverConfiguration;

    @Autowired(required = false)
    private ProblemProperties properties;

    @Test
    void contextLoadsWithoutProblemConfiguration() {
      assertThat(problemAutoConfiguration).isNull();
      assertThat(problemParameterConfiguration).isNull();
      assertThat(problemResolverConfiguration).isNull();

      assertThat(properties).isNull();
    }
  }
}
