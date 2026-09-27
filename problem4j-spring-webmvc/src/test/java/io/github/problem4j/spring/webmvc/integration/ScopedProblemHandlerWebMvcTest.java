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

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ScopedProblemHandlerWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @Test
  void givenExceptionFromControllerWithProblemHandler_whenRequesting_thenUsesControllerHandler() {
    ResponseEntity<String> response = restTemplate.getForEntity("/scoped-handler", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(jsonMapper.readValue(response.getBody(), Problem.class))
        .isEqualTo(Problem.builder().title("Scoped").status(409).build());
  }

  @Test
  void givenExceptionFromOtherController_whenRequesting_thenIgnoresControllerHandler() {
    ResponseEntity<String> response = restTemplate.getForEntity("/unscoped-handler", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void
      givenFrameworkExceptionFromControllerWithProblemHandler_whenRequesting_thenUsesControllerHandler() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/scoped-handler/missing-param", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(jsonMapper.readValue(response.getBody(), Problem.class))
        .isEqualTo(Problem.builder().title("Scoped missing parameter").status(422).build());
  }

  @Test
  void givenFrameworkExceptionFromOtherController_whenRequesting_thenUsesBuiltInResolver() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/unscoped-handler/missing-param", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
