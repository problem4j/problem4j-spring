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
import org.springframework.http.MediaType;

class ProblemMediaTypeResolverTest {

  @Test
  void givenAcceptJson_whenResolve_thenReturnsProblemJson() {
    MediaType resolved = ProblemMediaTypeResolver.resolve(List.of(MediaType.APPLICATION_JSON));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptXml_whenResolve_thenReturnsProblemXml() {
    MediaType resolved = ProblemMediaTypeResolver.resolve(List.of(MediaType.APPLICATION_XML));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenAcceptTextXml_whenResolve_thenReturnsProblemXml() {
    MediaType resolved = ProblemMediaTypeResolver.resolve(List.of(MediaType.TEXT_XML));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenAcceptProblemJson_whenResolve_thenReturnsProblemJson() {
    MediaType resolved =
        ProblemMediaTypeResolver.resolve(List.of(MediaType.APPLICATION_PROBLEM_JSON));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptProblemXml_whenResolve_thenReturnsProblemXml() {
    MediaType resolved =
        ProblemMediaTypeResolver.resolve(List.of(MediaType.APPLICATION_PROBLEM_XML));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenEmptyAccept_whenResolve_thenReturnsProblemJson() {
    MediaType resolved = ProblemMediaTypeResolver.resolve(List.of());

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptAll_whenResolve_thenReturnsProblemJson() {
    MediaType resolved = ProblemMediaTypeResolver.resolve(List.of(MediaType.ALL));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptUnrelatedType_whenResolve_thenReturnsProblemJson() {
    MediaType resolved = ProblemMediaTypeResolver.resolve(List.of(MediaType.TEXT_HTML));

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptXmlAndJsonWithHigherQuality_whenResolve_thenReturnsProblemJson() {
    List<MediaType> accepted =
        MediaType.parseMediaTypes("application/xml;q=0.5, application/json;q=0.9");

    MediaType resolved = ProblemMediaTypeResolver.resolve(accepted);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
  }

  @Test
  void givenAcceptJsonAndXmlWithHigherQuality_whenResolve_thenReturnsProblemXml() {
    List<MediaType> accepted =
        MediaType.parseMediaTypes("application/json;q=0.5, application/xml;q=0.9");

    MediaType resolved = ProblemMediaTypeResolver.resolve(accepted);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }

  @Test
  void givenAcceptXmlBeforeWildcard_whenResolve_thenReturnsProblemXml() {
    List<MediaType> accepted = MediaType.parseMediaTypes("application/xml, */*;q=0.8");

    MediaType resolved = ProblemMediaTypeResolver.resolve(accepted);

    assertThat(resolved).isEqualTo(MediaType.APPLICATION_PROBLEM_XML);
  }
}
