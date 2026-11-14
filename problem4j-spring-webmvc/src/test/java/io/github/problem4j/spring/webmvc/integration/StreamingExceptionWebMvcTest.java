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
import io.github.problem4j.spring.webmvc.app.problem.ResolvableException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@SpringBootTest(
    classes = {WebMvcTestApp.class, StreamingExceptionWebMvcTest.StreamingController.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.exception-handling.mode=cooperate",
      "problem4j.extension-overrides.processed=yes"
    })
@AutoConfigureTestRestTemplate
class StreamingExceptionWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void givenExceptionBeforeWriting_whenRequesting_thenRenderedAsPostProcessedProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/streaming-exception/before-write", String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getHeaders().getContentType()).hasToString("application/problem+json");
    assertThat(response.getBody()).contains("\"processed\":\"yes\"");
  }

  /**
   * Once the response is committed, Problem4J does not render the exception itself. The servlet
   * container then includes its error page into the response, so the problem rendered by {@code
   * ProblemErrorController} is appended, as the error body of Spring Boot's own error controller
   * would be.
   */
  @Test
  void givenExceptionAfterCommit_whenRequesting_thenStatusIsKeptAndErrorPageIsIncluded() {
    String[] response =
        restTemplate.execute(
            "/streaming-exception/after-commit",
            HttpMethod.GET,
            null,
            clientResponse ->
                new String[] {
                  String.valueOf(clientResponse.getStatusCode().value()),
                  read(clientResponse.getBody())
                });

    assertThat(response).isNotNull();
    assertThat(response[0]).isEqualTo("200");
    assertThat(response[1]).startsWith("xxx").endsWith("\"processed\":\"yes\"}");
  }

  private static String read(InputStream body) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (body) {
      body.transferTo(bytes);
    } catch (IOException e) {
      // the connection may be closed after the exception, keep what was received
    }
    return bytes.toString(StandardCharsets.UTF_8);
  }

  @RestController
  static class StreamingController {

    @GetMapping("/streaming-exception/before-write")
    StreamingResponseBody beforeWrite() {
      return out -> {
        throw new ResolvableException();
      };
    }

    @GetMapping("/streaming-exception/after-commit")
    StreamingResponseBody afterCommit() {
      return out -> {
        // larger than the response buffer, so the response is committed
        out.write("x".repeat(64 * 1024).getBytes(StandardCharsets.UTF_8));
        out.flush();
        throw new ResolvableException();
      };
    }
  }
}
