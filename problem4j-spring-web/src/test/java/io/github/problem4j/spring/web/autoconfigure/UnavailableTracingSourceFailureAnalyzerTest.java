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

package io.github.problem4j.spring.web.autoconfigure;

import static io.github.problem4j.spring.web.autoconfigure.TracingSourceConditions.UNAVAILABLE_SOURCE_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.diagnostics.FailureAnalysis;

class UnavailableTracingSourceFailureAnalyzerTest {

  private final UnavailableTracingSourceFailureAnalyzer analyzer =
      new UnavailableTracingSourceFailureAnalyzer();

  @Test
  void givenWrappedUnavailableTracingSourceException_whenAnalyzing_thenFailureIsDescribed() {
    UnavailableTracingSourceException cause = new UnavailableTracingSourceException();

    FailureAnalysis analysis =
        analyzer.analyze(new BeanCreationException("problemContextWebMvcFilter", "failed", cause));

    assertThat(analysis)
        .isNotNull()
        .returns(
            UnavailableTracingSourceFailureAnalyzer.DESCRIPTION, FailureAnalysis::getDescription)
        .returns(UnavailableTracingSourceFailureAnalyzer.ACTION, FailureAnalysis::getAction)
        .returns(cause, FailureAnalysis::getCause);
  }

  @Test
  void givenOtherException_whenAnalyzing_thenFailureIsNotDescribed() {
    FailureAnalysis analysis =
        analyzer.analyze(
            new BeanCreationException("bean", "failed", new IllegalStateException("other")));

    assertThat(analysis).isNull();
  }

  @Test
  void givenUnavailableTracingSourceException_whenCreated_thenMessageIsUnavailableSourceMessage() {
    assertThat(new UnavailableTracingSourceException())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(UNAVAILABLE_SOURCE_MESSAGE);
  }

  @Test
  void givenSpringFactories_whenLoaded_thenFailureAnalyzersAreRegistered() throws IOException {
    List<String> factories = new ArrayList<>();
    for (URL url :
        Collections.list(getClass().getClassLoader().getResources("META-INF/spring.factories"))) {
      try (InputStream stream = url.openStream()) {
        factories.add(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
      }
    }

    assertThat(factories)
        .anySatisfy(content ->
            assertThat(content)
                .contains("org.springframework.boot.diagnostics.FailureAnalyzer=")
                .contains(UnavailableTracingSourceFailureAnalyzer.class.getName())
                .contains(DuplicateProblemResolverFailureAnalyzer.class.getName()));
  }
}
