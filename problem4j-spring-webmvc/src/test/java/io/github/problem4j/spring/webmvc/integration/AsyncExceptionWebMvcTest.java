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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@SpringBootTest(
    classes = {WebMvcTestApp.class, AsyncExceptionWebMvcTest.AsyncController.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.exception-handling.mode=cooperate",
      "problem4j.extension-overrides.processed=yes",
      "spring.mvc.async.request-timeout=50ms"
    })
@AutoConfigureTestRestTemplate
class AsyncExceptionWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;

  @ParameterizedTest
  @CsvSource({
    "/async-exception/failed-future, 422",
    "/async-exception/failing-future, 422",
    "/async-exception/deferred-result-error, 422",
    "/async-exception/failing-callable, 422",
    "/async-exception/timeout, 503"
  })
  void givenExceptionOfAsyncRequest_whenRequesting_thenRenderedAsPostProcessedProblem(
      String path, int status) {
    ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(status);
    assertThat(response.getHeaders().getContentType()).hasToString("application/problem+json");
    assertThat(response.getBody()).contains("\"processed\":\"yes\"");
  }

  @RestController
  static class AsyncController {

    @GetMapping("/async-exception/failed-future")
    CompletableFuture<String> failedFuture() {
      return CompletableFuture.failedFuture(new ResolvableException());
    }

    @GetMapping("/async-exception/failing-future")
    CompletableFuture<String> failingFuture() {
      return CompletableFuture.supplyAsync(() -> {
        throw new ResolvableException();
      });
    }

    @GetMapping("/async-exception/deferred-result-error")
    DeferredResult<String> deferredResultError() {
      DeferredResult<String> result = new DeferredResult<>();
      result.setErrorResult(new ResolvableException());
      return result;
    }

    @GetMapping("/async-exception/failing-callable")
    Callable<String> failingCallable() {
      return () -> {
        throw new ResolvableException();
      };
    }

    @GetMapping("/async-exception/timeout")
    DeferredResult<String> timeout() {
      return new DeferredResult<>();
    }
  }
}
