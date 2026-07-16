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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ProblemContentTypeWebMvcTest {

  private static final Problem RESOLVABLE_PROBLEM =
      Problem.builder()
          .type("http://exception.example.org/resolvable")
          .title(ResolvableException.class.getSimpleName())
          .status(422)
          .extension("package", ResolvableException.class.getPackageName())
          .build();

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;
  @Autowired private XmlMapper xmlMapper;

  private ResponseEntity<String> getWithAccept(String url, MediaType... accept) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(accept));
    return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  @Test
  void givenAcceptJson_whenResolvableException_thenReturnsProblemJson() {
    ResponseEntity<String> response =
        getWithAccept("/problem-advice/resolvable", MediaType.APPLICATION_JSON);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem).isEqualTo(RESOLVABLE_PROBLEM);
  }

  @Test
  void givenAcceptXml_whenResolvableException_thenReturnsProblemXml() {
    ResponseEntity<String> response =
        getWithAccept("/problem-advice/resolvable", MediaType.APPLICATION_XML);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getHeaders().getContentType())
        .isNotNull()
        .satisfies(
            ct -> assertThat(ct.isCompatibleWith(MediaType.APPLICATION_PROBLEM_XML)).isTrue());

    Problem problem = xmlMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem).isEqualTo(RESOLVABLE_PROBLEM);
  }

  @Test
  void givenAcceptTextHtml_whenResolvableException_thenReturnsProblemJson() {
    ResponseEntity<String> response =
        getWithAccept("/problem-advice/resolvable", MediaType.TEXT_HTML);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
  }

  @Test
  void givenAcceptAll_whenResolvableException_thenReturnsProblemJson() {
    ResponseEntity<String> response = getWithAccept("/problem-advice/resolvable", MediaType.ALL);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
  }

  @Test
  void givenAcceptXml_whenProblemException_thenReturnsProblemXml() {
    ResponseEntity<String> response =
        getWithAccept(
            "/problem-advice/exception?value1=string1&value2=1&value3=true",
            MediaType.APPLICATION_XML);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(418));
    assertThat(response.getHeaders().getContentType())
        .isNotNull()
        .satisfies(
            ct -> assertThat(ct.isCompatibleWith(MediaType.APPLICATION_PROBLEM_XML)).isTrue());

    Problem problem = xmlMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .type("https://example.org/extended/string1")
                .title("Extended Exception")
                .status(418)
                .detail("value2:1")
                .instance("https://example.org/extended/instance/true")
                .build());
  }

  @Test
  void givenAcceptXml_whenFrameworkException_thenReturnsProblemXml() {
    ResponseEntity<String> response =
        getWithAccept("/type-mismatch/path-variable/abc", MediaType.APPLICATION_XML);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType())
        .isNotNull()
        .satisfies(
            ct -> assertThat(ct.isCompatibleWith(MediaType.APPLICATION_PROBLEM_XML)).isTrue());

    Problem problem = xmlMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void givenAcceptXml_whenSendError_thenReturnsProblemXml() {
    ResponseEntity<String> response =
        getWithAccept("/send-error/internal-server-error", MediaType.APPLICATION_XML);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getHeaders().getContentType())
        .isNotNull()
        .satisfies(
            ct -> assertThat(ct.isCompatibleWith(MediaType.APPLICATION_PROBLEM_XML)).isTrue());

    Problem problem = xmlMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem).isEqualTo(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @Test
  void givenAcceptXmlWithLowerQualityThanJson_whenResolvableException_thenReturnsProblemJson() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.ACCEPT, "application/xml;q=0.5, application/json;q=0.9");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/problem-advice/resolvable", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);
  }
}
