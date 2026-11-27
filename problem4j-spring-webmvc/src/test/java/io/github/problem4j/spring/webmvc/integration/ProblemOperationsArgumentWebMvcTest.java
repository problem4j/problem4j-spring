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

package io.github.problem4j.spring.webmvc.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ProblemOperationsArgumentWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/problem-operations/argument",
        "/problem-operations/template",
        "/problem-operations/optional"
      })
  void givenProblemOperationsParameter_whenRequesting_thenProblemTemplateIsResolved(String path) {
    ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("ProblemTemplate");
  }

  @Test
  void givenProblemOperationsParameter_whenPostProcessing_thenProblemIsReturned() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/problem-operations/post-process", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("418");
  }

  @Test
  void givenProblemOperationsParametersInExceptionHandler_whenRequesting_thenResolved() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/problem-operations/exception-handler", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("ProblemTemplate,ProblemTemplate,ProblemTemplate");
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/coroutine/problem-operations/argument|ProblemTemplate,ProblemTemplate",
        "/coroutine/problem-operations/plain-exception-handler|plain:ProblemTemplate"
      })
  void givenProblemOperationsParametersInCoroutineMethods_whenRequesting_thenResolved(
      String path, String expected) {
    ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(expected);
  }
}
