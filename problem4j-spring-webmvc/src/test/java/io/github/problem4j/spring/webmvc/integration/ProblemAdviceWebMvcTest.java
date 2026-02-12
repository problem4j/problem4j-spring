/*
 * Copyright (c) 2025-2026 Damian Malczewski
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
package io.github.problem4j.spring.webmvc.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemStatus;
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
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
        .isEqualTo(Problem.builder().status(ProblemStatus.INTERNAL_SERVER_ERROR).build());
  }
}
