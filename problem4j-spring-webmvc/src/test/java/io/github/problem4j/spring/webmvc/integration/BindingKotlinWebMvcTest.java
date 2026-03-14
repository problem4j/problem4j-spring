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

import static io.github.problem4j.spring.web.ProblemSupport.KIND_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.PROPERTY_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.TYPE_MISMATCH_DETAIL;
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
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class BindingKotlinWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/int     | { \"value\": null }",
        "/binding-kotlin/long    | { \"value\": null }",
        "/binding-kotlin/short   | { \"value\": null }",
        "/binding-kotlin/byte    | { \"value\": null }",
        "/binding-kotlin/float   | { \"value\": null }",
        "/binding-kotlin/double  | { \"value\": null }",
        "/binding-kotlin/boolean | { \"value\": null }"
      })
  void givenNullValue_whenPost_thenReturnTypeMismatch(String path, String json) {
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
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "value")
                .extension(KIND_EXTENSION, expectedKind)
                .build());
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/nested/int     | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/short   | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/float   | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/boolean | { \"nested\": { \"value\": null } }"
      })
  void givenNullInNestedObjectValue_whenPost_thenReturnTypeMismatch(String path, String json) {
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
    } else if (path.endsWith("/float")) {
      expectedKind = "number";
    } else {
      expectedKind = "integer";
    }

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "nested.value")
                .extension(KIND_EXTENSION, expectedKind)
                .build());
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/nested/int     | { \"nested\": null }",
        "/binding-kotlin/nested/int     | { }",
        "/binding-kotlin/nested/short   | { \"nested\": null }",
        "/binding-kotlin/nested/short   | { }",
        "/binding-kotlin/nested/float   | { \"nested\": null }",
        "/binding-kotlin/nested/float   | { }",
        "/binding-kotlin/nested/boolean | { \"nested\": null }",
        "/binding-kotlin/nested/boolean | { }"
      })
  void givenNullNestedObject_whenPost_thenReturnTypeMismatch(String path, String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(path, new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "nested")
                .build());
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
  void givenMalformedComplexKotlinObject_whenPost_thenReturnProblemWithFirstInvalidFieldAsProperty(
      String json, String expectedProperty, String expectedKind) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/binding-kotlin/complex", new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, expectedProperty)
                .extension(KIND_EXTENSION, expectedKind)
                .build());
  }

  @Test
  void givenNullableProperty_whenPostNull_thenReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/binding-kotlin/nullable",
            new HttpEntity<>("{ \"value\": null }", headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void givenDefaultParam_whenMissingProperty_thenUseDefault() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/binding-kotlin/default", new HttpEntity<>("{}", headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void givenListElementNullability_whenElementNull_thenBehaviorMatchesNullability() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> nonNullResponse =
        restTemplate.postForEntity(
            "/binding-kotlin/list/non-null-elements",
            new HttpEntity<>("{ \"values\": [null] }", headers),
            String.class);

    assertThat(nonNullResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Problem problem1 = jsonMapper.readValue(nonNullResponse.getBody(), Problem.class);
    assertThat(problem1)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "values")
                .extension(KIND_EXTENSION, "integer")
                .build());
  }

  @Test
  void givenListElementNullable_whenElementNull_thenReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> nullableResponse =
        restTemplate.postForEntity(
            "/binding-kotlin/list/nullable-elements",
            new HttpEntity<>("{ \"values\": [null] }", headers),
            String.class);

    assertThat(nullableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void givenMapValueNullability_whenValueNull_thenBehaviorMatchesNullability() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> nonNullResponse =
        restTemplate.postForEntity(
            "/binding-kotlin/map/non-null-values",
            new HttpEntity<>("{ \"map\": { \"k\": null } }", headers),
            String.class);

    assertThat(nonNullResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Problem problem2 = jsonMapper.readValue(nonNullResponse.getBody(), Problem.class);
    assertThat(problem2)
        .isEqualTo(
            Problem.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "map.k")
                .extension(KIND_EXTENSION, "integer")
                .build());
  }

  @Test
  void givenMapValueNullable_whenValueNull_thenReturnOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> nullableResponse =
        restTemplate.postForEntity(
            "/binding-kotlin/map/nullable-values",
            new HttpEntity<>("{ \"map\": { \"k\": null } }", headers),
            String.class);

    assertThat(nullableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/int     | { \"value\": 42 }",
        "/binding-kotlin/long    | { \"value\": 9223372036854775807 }",
        "/binding-kotlin/short   | { \"value\": 123 }",
        "/binding-kotlin/byte    | { \"value\": 12 }",
        "/binding-kotlin/float   | { \"value\": 3.14 }",
        "/binding-kotlin/double  | { \"value\": 2.71828 }",
        "/binding-kotlin/boolean | { \"value\": true }"
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
        "/binding-kotlin/int     | { \"value\": \"notInt\" }",
        "/binding-kotlin/int     | { \"value\": [\"notInt\"] }",
        "/binding-kotlin/int     | { \"value\": { \"notInt\": true } }",
        "/binding-kotlin/int     | { \"value\": null }",
        "/binding-kotlin/int     | { }",
        "/binding-kotlin/long    | { \"value\": \"notLong\" }",
        "/binding-kotlin/long    | { \"value\": [\"notLong\"] }",
        "/binding-kotlin/long    | { \"value\": { \"notLong\": true } }",
        "/binding-kotlin/long    | { \"value\": null }",
        "/binding-kotlin/long    | { }",
        "/binding-kotlin/short   | { \"value\": \"notShort\" }",
        "/binding-kotlin/short   | { \"value\": [\"notShort\"] }",
        "/binding-kotlin/short   | { \"value\": { \"notShort\":true } }",
        "/binding-kotlin/short   | { \"value\": null }",
        "/binding-kotlin/short   | { }",
        "/binding-kotlin/byte    | { \"value\": \"notByte\" }",
        "/binding-kotlin/byte    | { \"value\": [\"notByte\"] }",
        "/binding-kotlin/byte    | { \"value\": { \"notByte\": true } }",
        "/binding-kotlin/byte    | { \"value\": null }",
        "/binding-kotlin/byte    | { }",
        "/binding-kotlin/float   | { \"value\": \"notFloat\" }",
        "/binding-kotlin/float   | { \"value\": [\"notFloat\"] }",
        "/binding-kotlin/float   | { \"value\": { \"notFloat\": true } }",
        "/binding-kotlin/float   | { \"value\": null }",
        "/binding-kotlin/float   | { }",
        "/binding-kotlin/double  | { \"value\": \"notDouble\" }",
        "/binding-kotlin/double  | { \"value\": [\"notDouble\"] }",
        "/binding-kotlin/double  | { \"value\": { \"notDouble\": true } }",
        "/binding-kotlin/double  | { \"value\": null }",
        "/binding-kotlin/double  | { }",
        "/binding-kotlin/boolean | { \"value\": \"notBool\" }",
        "/binding-kotlin/boolean | { \"value\": [\"notBool\"] }",
        "/binding-kotlin/boolean | { \"value\": { \"notBool\": true } }",
        "/binding-kotlin/boolean | { \"value\": null }",
        "/binding-kotlin/boolean | { }",
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
            .detail(TYPE_MISMATCH_DETAIL)
            .extension(PROPERTY_EXTENSION, "value")
            .extension(KIND_EXTENSION, expectedKind)
            .build();

    if (!problem.equals(expected)) {
      assertThat(problem).isEqualTo(Problem.of(HttpStatus.BAD_REQUEST.value()));
    }
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/nested/int     | { \"nested\": { \"value\": 42 } }",
        "/binding-kotlin/nested/long    | { \"nested\": { \"value\": 9223372036854775807 } }",
        "/binding-kotlin/nested/short   | { \"nested\": { \"value\": 123 } }",
        "/binding-kotlin/nested/byte    | { \"nested\": { \"value\": 12 } }",
        "/binding-kotlin/nested/float   | { \"nested\": { \"value\": 3.14 } }",
        "/binding-kotlin/nested/double  | { \"nested\": { \"value\": 2.71828 } }",
        "/binding-kotlin/nested/boolean | { \"nested\": { \"value\": true } }"
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
        "/binding-kotlin/nested/int     | { \"nested\": { \"value\": \"notInt\" } }",
        "/binding-kotlin/nested/int     | { \"nested\": { \"value\": [\"notInt\"] } }",
        "/binding-kotlin/nested/int     | { \"nested\": { \"value\": { \"notInt\": true } } }",
        "/binding-kotlin/nested/int     | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/int     | { \"nested\": { } } }",
        "/binding-kotlin/nested/long    | { \"nested\": { \"value\": \"notLong\" } }",
        "/binding-kotlin/nested/long    | { \"nested\": { \"value\": [\"notLong\"] } }",
        "/binding-kotlin/nested/long    | { \"nested\": { \"value\": { \"notLong\": true } } }",
        "/binding-kotlin/nested/long    | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/long    | { \"nested\": { } }",
        "/binding-kotlin/nested/short   | { \"nested\": { \"value\": \"notShort\" } }",
        "/binding-kotlin/nested/short   | { \"nested\": { \"value\": [\"notShort\"] } }",
        "/binding-kotlin/nested/short   | { \"nested\": { \"value\": { \"notShort\":true } } }",
        "/binding-kotlin/nested/short   | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/short   | { \"nested\": { } }",
        "/binding-kotlin/nested/byte    | { \"nested\": { \"value\": \"notByte\" } }",
        "/binding-kotlin/nested/byte    | { \"nested\": { \"value\": [\"notByte\"] } }",
        "/binding-kotlin/nested/byte    | { \"nested\": { \"value\": { \"notByte\": true } } }",
        "/binding-kotlin/nested/byte    | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/byte    | { \"nested\": { } }",
        "/binding-kotlin/nested/float   | { \"nested\": { \"value\": \"notFloat\" } }",
        "/binding-kotlin/nested/float   | { \"nested\": { \"value\": [\"notFloat\"] } }",
        "/binding-kotlin/nested/float   | { \"nested\": { \"value\": { \"notFloat\": true } } }",
        "/binding-kotlin/nested/float   | { \"nested\": { \"value\": null } }",
        "/binding-kotlin/nested/float   | { \"nested\": { } }",
        "/binding-kotlin/nested/double  | { \"nested\": { \"value\": \"notDouble\" } }",
        "/binding-kotlin/nested/double  | { \"nested\": { \"value\": [\"notDouble\"] } }",
        "/binding-kotlin/nested/double  | { \"nested\": { \"value\": { \"notDouble\": true } } }",
        "/binding-kotlin/nested/double  | { \"nested\": { \"value\": { \"notDouble\": null } } }",
        "/binding-kotlin/nested/double  | { \"nested\": { \"value\": { } } }",
        "/binding-kotlin/nested/boolean | { \"nested\": { \"value\": \"notBool\" } }",
        "/binding-kotlin/nested/boolean | { \"nested\": { \"value\": [\"notBool\"] } }",
        "/binding-kotlin/nested/boolean | { \"nested\": { \"value\": { \"notBool\": true } } }",
        "/binding-kotlin/nested/boolean | { \"nested\": { \"value\": { \"notBool\": null } } }",
        "/binding-kotlin/nested/boolean | { \"nested\": { \"value\": { } } }",
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
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "nested.value")
                .extension(KIND_EXTENSION, expectedKind)
                .build());
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/int     | { \"value\": \"\" }",
        "/binding-kotlin/long    | { \"value\": \"\" }",
        "/binding-kotlin/short   | { \"value\": \"\" }",
        "/binding-kotlin/byte    | { \"value\": \"\" }",
        "/binding-kotlin/float   | { \"value\": \"\" }",
        "/binding-kotlin/double  | { \"value\": \"\" }",
        "/binding-kotlin/boolean | { \"value\": \"\" }",
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
                .detail(TYPE_MISMATCH_DETAIL)
                .extension(PROPERTY_EXTENSION, "value")
                .extension(KIND_EXTENSION, expectedKind)
                .build());
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "/binding-kotlin/int     | { \"value\": 2147483648 }",
        "/binding-kotlin/long    | { \"value\": 9223372036854775808 }",
        "/binding-kotlin/short   | { \"value\": 40000 }",
        "/binding-kotlin/byte    | { \"value\": 256 }",
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
            .detail(TYPE_MISMATCH_DETAIL)
            .extension(PROPERTY_EXTENSION, "value")
            .extension(KIND_EXTENSION, "integer")
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
          "shortNested": { "value": 3 },
          "tree": { "leaf": { "nested": { "value": 3 } } }
        }
        """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/binding-kotlin/complex", new HttpEntity<>(json, headers), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");
  }
}
