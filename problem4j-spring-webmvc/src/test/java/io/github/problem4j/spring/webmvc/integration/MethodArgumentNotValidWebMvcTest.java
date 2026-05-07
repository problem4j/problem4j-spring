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

package io.github.problem4j.spring.webmvc.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import java.util.List;
import java.util.Map;
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
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
@AutoConfigureTestRestTemplate
class MethodArgumentNotValidWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @Test
  void givenModelAttributeTypeMismatch_shouldReturnBadRequestProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/method-argument-not-valid?number=abc", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("validation failed")
                .extension("errors", List.of(Map.of("field", "number", "error", "is not valid")))
                .build());
  }
}
