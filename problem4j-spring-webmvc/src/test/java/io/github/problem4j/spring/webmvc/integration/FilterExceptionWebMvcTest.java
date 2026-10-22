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
import io.github.problem4j.spring.webmvc.app.problem.AnnotatedException;
import io.github.problem4j.spring.webmvc.app.problem.ResolvableException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"problem4j.detail-format=lowercase"})
@AutoConfigureTestRestTemplate
@Import(FilterExceptionWebMvcTest.ThrowingFilterConfiguration.class)
class FilterExceptionWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @Test
  void givenResolvableExceptionThrownFromFilter_whenRequesting_thenResolverIsUsed() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/filter-exception/resolvable", String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE_JSON);

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
  void givenProblemMappingExceptionThrownFromFilter_whenRequesting_thenMappingIsUsed() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/filter-exception/annotated", String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(418);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .type("https://example.org/annotated/abc")
                .title("Annotated Exception")
                .status(418)
                .detail("value2:123")
                .instance("https://example.org/annotated/instance/true")
                .build());
  }

  @Test
  void givenUnknownExceptionThrownFromFilter_whenRequesting_thenInternalServerErrorIsReturned() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/filter-exception/unknown", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem).isEqualTo(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @TestConfiguration
  static class ThrowingFilterConfiguration {

    @Bean
    OncePerRequestFilter throwingFilter() {
      return new OncePerRequestFilter() {
        @Override
        protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
          switch (request.getRequestURI()) {
            case "/filter-exception/resolvable" -> throw new ResolvableException();
            case "/filter-exception/annotated" -> throw new AnnotatedException("abc", 123L, true);
            case "/filter-exception/unknown" -> throw new IllegalStateException("unknown");
            default -> filterChain.doFilter(request, response);
          }
        }
      };
    }
  }
}
