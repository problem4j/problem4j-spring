/*
 * Copyright 2025-2026 The Problem4J Authors
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.jackson.deserialization.fail-on-null-for-primitives=true",
      "problem4j.detail-format=lowercase"
    })
@AutoConfigureTestRestTemplate
class BindingPrimitiveWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-primitive/int     | { \"value\": 42 }",
        "/binding-primitive/long    | { \"value\": 9223372036854775807 }",
        "/binding-primitive/short   | { \"value\": 123 }",
        "/binding-primitive/byte    | { \"value\": 12 }",
        "/binding-primitive/float   | { \"value\": 3.14 }",
        "/binding-primitive/double  | { \"value\": 2.71828 }",
        "/binding-primitive/boolean | { \"value\": true }"
      })
  void givenValidPrimitive_whenPost_thenReturnOk(String path, String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(path, new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-primitive/int     | { \"value\": \"notInt\" }",
        "/binding-primitive/int     | { \"value\": [\"notInt\"] }",
        "/binding-primitive/int     | { \"value\": { \"notInt\": true } }",
        "/binding-primitive/int     | { \"value\": null }",
        "/binding-primitive/int     | { }",
        "/binding-primitive/long    | { \"value\": \"notLong\" }",
        "/binding-primitive/long    | { \"value\": [\"notLong\"] }",
        "/binding-primitive/long    | { \"value\": { \"notLong\": true } }",
        "/binding-primitive/long    | { \"value\": null }",
        "/binding-primitive/long    | { }",
        "/binding-primitive/short   | { \"value\": \"notShort\" }",
        "/binding-primitive/short   | { \"value\": [\"notShort\"] }",
        "/binding-primitive/short   | { \"value\": { \"notShort\":true } }",
        "/binding-primitive/short   | { \"value\": null }",
        "/binding-primitive/short   | { }",
        "/binding-primitive/byte    | { \"value\": \"notByte\" }",
        "/binding-primitive/byte    | { \"value\": [\"notByte\"] }",
        "/binding-primitive/byte    | { \"value\": { \"notByte\": true } }",
        "/binding-primitive/byte    | { \"value\": null }",
        "/binding-primitive/byte    | { }",
        "/binding-primitive/float   | { \"value\": \"notFloat\" }",
        "/binding-primitive/float   | { \"value\": [\"notFloat\"] }",
        "/binding-primitive/float   | { \"value\": { \"notFloat\": true } }",
        "/binding-primitive/float   | { \"value\": null }",
        "/binding-primitive/float   | { }",
        "/binding-primitive/double  | { \"value\": \"notDouble\" }",
        "/binding-primitive/double  | { \"value\": [\"notDouble\"] }",
        "/binding-primitive/double  | { \"value\": { \"notDouble\": true } }",
        "/binding-primitive/double  | { \"value\": null }",
        "/binding-primitive/double  | { }",
        "/binding-primitive/boolean | { \"value\": \"notBool\" }",
        "/binding-primitive/boolean | { \"value\": [\"notBool\"] }",
        "/binding-primitive/boolean | { \"value\": { \"notBool\": true } }",
        "/binding-primitive/boolean | { \"value\": null }",
        "/binding-primitive/boolean | { }",
      })
  void givenMalformedPrimitive_whenPost_thenReturnProblem(String path, String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(path, new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    String expectedKind;
    if (path.endsWith("/boolean")) {
      expectedKind = "boolean";
    } else if (path.endsWith("/float") || path.endsWith("/double")) {
      expectedKind = "number";
    } else {
      expectedKind = "integer";
    }

    Problem expected =
        Problem.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("type mismatch")
            .extension("property", "value")
            .extension("kind", expectedKind)
            .build();

    if (!problem.equals(expected)) {
      assertThat(problem).isEqualTo(Problem.of(HttpStatus.BAD_REQUEST.value()));
    }
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-primitive/nested/int     | { \"value\": { \"value\": 42 } }",
        "/binding-primitive/nested/long    | { \"value\": { \"value\": 9223372036854775807 } }",
        "/binding-primitive/nested/short   | { \"value\": { \"value\": 123 } }",
        "/binding-primitive/nested/byte    | { \"value\": { \"value\": 12 } }",
        "/binding-primitive/nested/float   | { \"value\": { \"value\": 3.14 } }",
        "/binding-primitive/nested/double  | { \"value\": { \"value\": 2.71828 } }",
        "/binding-primitive/nested/boolean | { \"value\": { \"value\": true } }"
      })
  void givenValidNested_whenPost_thenReturnOk(String path, String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(path, new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-primitive/nested/int     | { \"nested\": { \"value\": \"notInt\" } }",
        "/binding-primitive/nested/int     | { \"nested\": { \"value\": [\"notInt\"] } }",
        "/binding-primitive/nested/int     | { \"nested\": { \"value\": { \"notInt\": true } } }",
        "/binding-primitive/nested/int     | { \"nested\": { \"value\": null } }",
        "/binding-primitive/nested/int     | { \"nested\": { } } }",
        "/binding-primitive/nested/long    | { \"nested\": { \"value\": \"notLong\" } }",
        "/binding-primitive/nested/long    | { \"nested\": { \"value\": [\"notLong\"] } }",
        "/binding-primitive/nested/long    | { \"nested\": { \"value\": { \"notLong\": true } } }",
        "/binding-primitive/nested/long    | { \"nested\": { \"value\": null } }",
        "/binding-primitive/nested/long    | { \"nested\": { } }",
        "/binding-primitive/nested/short   | { \"nested\": { \"value\": \"notShort\" } }",
        "/binding-primitive/nested/short   | { \"nested\": { \"value\": [\"notShort\"] } }",
        "/binding-primitive/nested/short   | { \"nested\": { \"value\": { \"notShort\":true } } }",
        "/binding-primitive/nested/short   | { \"nested\": { \"value\": null } }",
        "/binding-primitive/nested/short   | { \"nested\": { } }",
        "/binding-primitive/nested/byte    | { \"nested\": { \"value\": \"notByte\" } }",
        "/binding-primitive/nested/byte    | { \"nested\": { \"value\": [\"notByte\"] } }",
        "/binding-primitive/nested/byte    | { \"nested\": { \"value\": { \"notByte\": true } } }",
        "/binding-primitive/nested/byte    | { \"nested\": { \"value\": null } }",
        "/binding-primitive/nested/byte    | { \"nested\": { } }",
        "/binding-primitive/nested/float   | { \"nested\": { \"value\": \"notFloat\" } }",
        "/binding-primitive/nested/float   | { \"nested\": { \"value\": [\"notFloat\"] } }",
        "/binding-primitive/nested/float   | { \"nested\": { \"value\": { \"notFloat\": true } } }",
        "/binding-primitive/nested/float   | { \"nested\": { \"value\": null } }",
        "/binding-primitive/nested/float   | { \"nested\": { } }",
        "/binding-primitive/nested/double  | { \"nested\": { \"value\": \"notDouble\" } }",
        "/binding-primitive/nested/double  | { \"nested\": { \"value\": [\"notDouble\"] } }",
        "/binding-primitive/nested/double  | { \"nested\": { \"value\": { \"notDouble\": true } } }",
        "/binding-primitive/nested/double  | { \"nested\": { \"value\": { \"notDouble\": null } } }",
        "/binding-primitive/nested/double  | { \"nested\": { \"value\": { } } }",
        "/binding-primitive/nested/boolean | { \"nested\": { \"value\": \"notBool\" } }",
        "/binding-primitive/nested/boolean | { \"nested\": { \"value\": [\"notBool\"] } }",
        "/binding-primitive/nested/boolean | { \"nested\": { \"value\": { \"notBool\": true } } }",
        "/binding-primitive/nested/boolean | { \"nested\": { \"value\": { \"notBool\": null } } }",
        "/binding-primitive/nested/boolean | { \"nested\": { \"value\": { } } }",
      })
  void givenMalformedNested_whenPost_thenReturnProblem(String path, String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(path, new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    String expectedKind;
    if (path.endsWith("/boolean")) {
      expectedKind = "boolean";
    } else if (path.endsWith("/float") || path.endsWith("/double")) {
      expectedKind = "number";
    } else {
      expectedKind = "integer";
    }

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("type mismatch")
                .extension("property", "nested.value")
                .extension("kind", expectedKind)
                .build());
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-primitive/int     | { \"value\": \"\" }",
        "/binding-primitive/long    | { \"value\": \"\" }",
        "/binding-primitive/short   | { \"value\": \"\" }",
        "/binding-primitive/byte    | { \"value\": \"\" }",
        "/binding-primitive/float   | { \"value\": \"\" }",
        "/binding-primitive/double  | { \"value\": \"\" }",
        "/binding-primitive/boolean | { \"value\": \"\" }",
      })
  void givenEmptyStringPrimitive_whenPost_thenReturnProblem(String path, String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(path, new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    String expectedKind;
    if (path.endsWith("/boolean")) {
      expectedKind = "boolean";
    } else if (path.endsWith("/float") || path.endsWith("/double")) {
      expectedKind = "number";
    } else {
      expectedKind = "integer";
    }

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("type mismatch")
                .extension("property", "value")
                .extension("kind", expectedKind)
                .build());
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-primitive/int     | { \"value\": 2147483648 }",
        "/binding-primitive/long    | { \"value\": 9223372036854775808 }",
        "/binding-primitive/short   | { \"value\": 40000 }",
        "/binding-primitive/byte    | { \"value\": 256 }",
      })
  void givenOverflowPrimitive_whenPost_thenReturnProblem(String path, String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(path, new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    Problem expected =
        Problem.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("type mismatch")
            .extension("property", "value")
            .extension("kind", "integer")
            .build();

    if (!problem.equals(expected)) {
      assertThat(problem).isEqualTo(Problem.of(HttpStatus.BAD_REQUEST.value()));
    }
  }

  @Test
  void givenNullPrimitive_whenPost_thenReturnProblem() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/binding-primitive/int",
            new HttpEntity<>("{ \"value\": null }", headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    Problem expected =
        Problem.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("type mismatch")
            .extension("property", "value")
            .extension("kind", "integer")
            .build();

    if (!problem.equals(expected)) {
      assertThat(problem).isEqualTo(Problem.of(HttpStatus.BAD_REQUEST.value()));
    }
  }

  @Test
  void givenValidComplexRoot_whenPost_thenReturnOk() {
    String json =
        """
        {
          "flag": true,
          "timestamp": 1672531200000,
          "amount": 12.34,
          "shortNested": { "value": 3 }
        }
        """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/binding-primitive/complex", new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "{ \"flag\": \"notBool\", \"timestamp\": \"notLong\", \"amount\": \"notFloat\", \"shortNested\": { \"value\": \"notShort\" } } | flag              | boolean",
        "{ \"timestamp\": \"notLong\", \"flag\": \"notBool\", \"amount\": \"notFloat\", \"shortNested\": { \"value\": \"notShort\" } } | timestamp         | integer",
        "{ \"amount\": \"notFloat\", \"flag\": \"notBool\", \"timestamp\": \"notLong\", \"shortNested\": { \"value\": \"notShort\" } } | amount            | number",
        "{ \"shortNested\": { \"value\": \"notShort\" }, \"flag\": \"notBool\", \"timestamp\": \"notLong\", \"amount\": \"notFloat\" } | shortNested.value | integer"
      })
  void givenMalformedComplexObject_whenPost_thenReturnProblemWithFirstInvalidFieldAsProperty(
      String json, String expectedProperty, String expectedKind) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/binding-primitive/complex", new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("type mismatch")
                .extension("property", expectedProperty)
                .extension("kind", expectedKind)
                .build());
  }
}
