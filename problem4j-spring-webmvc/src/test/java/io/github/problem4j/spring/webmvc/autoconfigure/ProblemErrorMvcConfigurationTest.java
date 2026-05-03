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

package io.github.problem4j.spring.webmvc.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.webmvc.ProblemErrorController;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.error.ErrorController;

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
