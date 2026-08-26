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

package io.github.problem4j.spring.webflux.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.webflux.app.WebFluxTestApp;
import io.github.problem4j.spring.webflux.app.XmlCodecTestConfiguration;
import io.github.problem4j.spring.webflux.app.problem.ResolvableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import tools.jackson.dataformat.xml.XmlMapper;

@SpringBootTest(
    classes = {WebFluxTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(XmlCodecTestConfiguration.class)
class ProblemContentTypeWebFluxTest {

  private static final Problem RESOLVABLE_PROBLEM =
      Problem.builder()
          .type("http://exception.example.org/resolvable")
          .title(ResolvableException.class.getSimpleName())
          .status(422)
          .extension("package", ResolvableException.class.getPackageName())
          .build();

  @Autowired private WebTestClient webTestClient;
  @Autowired private XmlMapper xmlMapper;

  @Test
  void givenAcceptJson_whenResolvableException_thenReturnsProblemJson() {
    webTestClient
        .get()
        .uri("/problem-advice/resolvable")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE)
        .expectBody(Problem.class)
        .isEqualTo(RESOLVABLE_PROBLEM);
  }

  @Test
  void givenAcceptXml_whenResolvableException_thenReturnsProblemXml() {
    webTestClient
        .get()
        .uri("/problem-advice/resolvable")
        .accept(MediaType.APPLICATION_XML)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_XML)
        .expectBody(String.class)
        .value(
            body ->
                assertThat(xmlMapper.readValue(body, Problem.class)).isEqualTo(RESOLVABLE_PROBLEM));
  }

  @Test
  void givenAcceptTextHtml_whenResolvableException_thenReturnsProblemJson() {
    webTestClient
        .get()
        .uri("/problem-advice/resolvable")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE);
  }

  @Test
  void givenAcceptAll_whenResolvableException_thenReturnsProblemJson() {
    webTestClient
        .get()
        .uri("/problem-advice/resolvable")
        .accept(MediaType.ALL)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE);
  }

  @Test
  void givenAcceptXml_whenProblemException_thenReturnsProblemXml() {
    webTestClient
        .get()
        .uri("/problem-advice/exception?value1=string1&value2=1&value3=true")
        .accept(MediaType.APPLICATION_XML)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.valueOf(418))
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_XML)
        .expectBody(String.class)
        .value(
            body ->
                assertThat(xmlMapper.readValue(body, Problem.class))
                    .isEqualTo(
                        Problem.builder()
                            .type("https://example.org/extended/string1")
                            .title("Extended Exception")
                            .status(418)
                            .detail("value2:1")
                            .instance("https://example.org/extended/instance/true")
                            .build()));
  }

  @Test
  void givenAcceptXml_whenFrameworkException_thenReturnsProblemXml() {
    webTestClient
        .get()
        .uri("/type-mismatch/path-variable/abc")
        .accept(MediaType.APPLICATION_XML)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST)
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_XML)
        .expectBody(String.class)
        .value(
            body ->
                assertThat(xmlMapper.readValue(body, Problem.class).getStatus())
                    .isEqualTo(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void givenAcceptXml_whenFilterError_thenReturnsProblemXml() {
    webTestClient
        .get()
        .uri("/error-handler/no-context")
        .accept(MediaType.APPLICATION_XML)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_XML)
        .expectBody(String.class)
        .value(
            body ->
                assertThat(xmlMapper.readValue(body, Problem.class))
                    .isEqualTo(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value())));
  }

  @Test
  void givenAcceptXmlWithLowerQualityThanJson_whenResolvableException_thenReturnsProblemJson() {
    webTestClient
        .get()
        .uri("/problem-advice/resolvable")
        .header("Accept", "application/xml;q=0.5, application/json;q=0.9")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
        .expectHeader()
        .contentType(Problem.CONTENT_TYPE);
  }
}
