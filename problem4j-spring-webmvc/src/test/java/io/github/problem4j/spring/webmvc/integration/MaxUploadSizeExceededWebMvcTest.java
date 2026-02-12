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

import static io.github.problem4j.spring.web.ProblemSupport.MAX_UPLOAD_SIZE_EXCEEDED_DETAIL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {WebMvcTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.servlet.multipart.max-file-size=1KB",
      "spring.servlet.multipart.max-request-size=1KB"
    })
@AutoConfigureTestRestTemplate
class MaxUploadSizeExceededWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JsonMapper jsonMapper;

  @Test
  void givenMaxUploadSizeExceeded_shouldReturnProblem() {
    MultiValueMap<String, Object> body = prepareMultipartBody();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity("/max-upload-size-exceeded", requestEntity, String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE.value());
    assertThat(response.getHeaders().getContentType()).hasToString(Problem.CONTENT_TYPE);

    Problem problem = jsonMapper.readValue(response.getBody(), Problem.class);
    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(ProblemStatus.CONTENT_TOO_LARGE)
                .detail(MAX_UPLOAD_SIZE_EXCEEDED_DETAIL)
                .build());
  }

  private MultiValueMap<String, Object> prepareMultipartBody() {
    byte[] largeFile = new byte[1024 * 2];
    ByteArrayResource resource =
        new ByteArrayResource(largeFile) {
          @Override
          public String getFilename() {
            return "large-file.txt";
          }
        };

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", resource);
    return body;
  }
}
