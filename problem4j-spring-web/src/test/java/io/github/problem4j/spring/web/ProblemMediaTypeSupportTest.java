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

package io.github.problem4j.spring.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;

class ProblemMediaTypeSupportTest {

  @Test
  void givenAcceptJson_whenResolve_thenReturnsProblemJson() {
    MediaType resolved =
        ProblemMediaTypeSupport.resolveAccepted(List.of(MediaType.APPLICATION_JSON));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptXml_whenResolve_thenReturnsProblemXml() {
    MediaType resolved =
        ProblemMediaTypeSupport.resolveAccepted(List.of(MediaType.APPLICATION_XML));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenAcceptTextXml_whenResolve_thenReturnsProblemXml() {
    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(List.of(MediaType.TEXT_XML));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenAcceptProblemJson_whenResolve_thenReturnsProblemJson() {
    MediaType resolved =
        ProblemMediaTypeSupport.resolveAccepted(List.of(MediaType.APPLICATION_PROBLEM_JSON));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptProblemXml_whenResolve_thenReturnsProblemXml() {
    MediaType resolved =
        ProblemMediaTypeSupport.resolveAccepted(List.of(MediaType.APPLICATION_PROBLEM_XML));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenEmptyAccept_whenResolve_thenReturnsProblemJson() {
    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(List.of());

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptAll_whenResolve_thenReturnsProblemJson() {
    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(List.of(MediaType.ALL));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptUnrelatedType_whenResolve_thenReturnsProblemJson() {
    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(List.of(MediaType.TEXT_HTML));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptXmlAndJsonWithHigherQuality_whenResolve_thenReturnsProblemJson() {
    List<MediaType> accepted =
        MediaType.parseMediaTypes("application/xml;q=0.5, application/json;q=0.9");

    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(accepted);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptJsonAndXmlWithHigherQuality_whenResolve_thenReturnsProblemXml() {
    List<MediaType> accepted =
        MediaType.parseMediaTypes("application/json;q=0.5, application/xml;q=0.9");

    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(accepted);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenAcceptXmlBeforeWildcard_whenResolve_thenReturnsProblemXml() {
    List<MediaType> accepted = MediaType.parseMediaTypes("application/xml, */*;q=0.8");

    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(accepted);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenWildcardSubtype_whenResolve_thenReturnsProblemJson() {
    MediaType resolved =
        ProblemMediaTypeSupport.resolveAccepted(List.of(new MediaType("application")));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenSingleAcceptedMediaTypeArgument_whenResolve_thenReturnsProblemXml() {
    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(MediaType.APPLICATION_XML);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenVarargsAcceptedMediaTypes_whenResolve_thenReturnsProblemXml() {
    MediaType resolved =
        ProblemMediaTypeSupport.resolveAccepted(MediaType.TEXT_HTML, MediaType.APPLICATION_XML);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenNoVarargsAcceptedMediaTypes_whenResolve_thenReturnsProblemJson() {
    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted();

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @ParameterizedTest
  @CsvSource({
    "application/xml, application/problem+xml",
    "text/xml, application/problem+xml",
    "application/problem+xml, application/problem+xml",
    "application/soap+xml, application/problem+xml",
    "application/atom+xml, application/problem+xml",
    "application/rss+xml, application/problem+xml",
    "application/xhtml+xml, application/problem+xml"
  })
  void givenAnyXmlSubtype_whenResolve_thenReturnsProblemXml(String accept, String expected) {
    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(MediaType.valueOf(accept));

    assertThat(resolved).isEqualTo(MediaType.valueOf(expected));
  }

  @ParameterizedTest
  @CsvSource({
    "application/json, application/problem+json",
    "application/problem+json, application/problem+json",
    "application/vnd.api+json, application/problem+json",
    "application/ld+json, application/problem+json",
    "application/hal+json, application/problem+json",
    "application/merge-patch+json, application/problem+json"
  })
  void givenAnyJsonSubtype_whenResolve_thenReturnsProblemJson(String accept, String expected) {
    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(MediaType.valueOf(accept));

    assertThat(resolved).isEqualTo(MediaType.valueOf(expected));
  }

  @ParameterizedTest
  @CsvSource({
    "APPLICATION/XML, application/problem+xml",
    "Application/Soap+Xml, application/problem+xml",
    "APPLICATION/JSON, application/problem+json",
    "Application/Vnd.Api+Json, application/problem+json"
  })
  void givenMixedCaseSubtype_whenResolve_thenReturnsExpectedType(String accept, String expected) {
    MediaType resolved = ProblemMediaTypeSupport.resolveAccepted(MediaType.valueOf(accept));

    assertThat(resolved).isEqualTo(MediaType.valueOf(expected));
  }
}
