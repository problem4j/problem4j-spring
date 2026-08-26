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

package io.github.problem4j.spring.webmvc;

import static io.github.problem4j.spring.webmvc.WebMvcAdviceSupport.resolveContentType;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

class WebMvcAdviceSupportTest {

  @Test
  void givenAcceptXml_whenResolveContentTypeFromWebRequest_thenReturnsProblemXml() {
    WebRequest request = webRequest(MediaType.APPLICATION_XML);

    MediaType resolved = resolveContentType(request);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenNoAcceptHeader_whenResolveContentTypeFromWebRequest_thenReturnsProblemJson() {
    WebRequest request =
        new ServletWebRequest(
            new MockHttpServletRequest("GET", "/test"), new MockHttpServletResponse());

    MediaType resolved = resolveContentType(request);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptXml_whenResolveContentTypeFromServletRequest_thenReturnsProblemXml() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
    request.addHeader("Accept", MediaType.APPLICATION_XML_VALUE);

    MediaType resolved = resolveContentType(request);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenNoAcceptHeader_whenResolveContentTypeFromServletRequest_thenReturnsProblemJson() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");

    MediaType resolved = resolveContentType(request);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  private static WebRequest webRequest(MediaType accept) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
    request.addHeader("Accept", accept.toString());
    return new ServletWebRequest(request, new MockHttpServletResponse());
  }
}
