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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import io.github.problem4j.spring.webmvc.app.problem.ResolvableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
@AutoConfigureTestRestTemplate
class ProblemAdviceWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @ParameterizedTest
  @CsvSource({
    "string1, 1, true",
    "string2, 2, false",
  })
  void givenExtendedProblemException_shouldReturnProblem(
      String value1, Long value2, boolean value3) {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/problem-advice/exception?value1="
                + value1
                + "&value2="
                + value2
                + "&value3="
                + value3,
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(418));
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .type("https://example.org/extended/" + value1)
                .title("Extended Exception")
                .status(418)
                .detail("value2:" + value2)
                .instance("https://example.org/extended/instance/" + value3)
                .build());
  }

  @ParameterizedTest
  @CsvSource({
    "string1, 1, true",
    "string2, 2, false",
  })
  void givenAnnotatedException_shouldReturnProblem(String value1, Long value2, boolean value3) {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/problem-advice/annotation?value1="
                + value1
                + "&value2="
                + value2
                + "&value3="
                + value3,
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(418));
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .type("https://example.org/annotated/" + value1)
                .title("Annotated Exception")
                .status(418)
                .detail("value2:" + value2)
                .instance("https://example.org/annotated/instance/" + value3)
                .build());
  }

  @Test
  void givenAnnotationEmptyException_returnProblemWithUnknownStatus() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/problem-advice/annotation-empty", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem).isEqualTo(Problem.builder().status(0).build());
  }

  @Test
  void givenResolvableException_shouldReturnProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/problem-advice/resolvable", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .type("http://exception.example.org/resolvable")
                .title(ResolvableException.class.getSimpleName())
                .status(422)
                .extension("package", ResolvableException.class.getPackageName())
                .build());
  }

  @Test
  void givenUnresolvableException_shouldReturnInternalServerErrorProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/problem-advice/unresolvable", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(Problem.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build());
  }
}
