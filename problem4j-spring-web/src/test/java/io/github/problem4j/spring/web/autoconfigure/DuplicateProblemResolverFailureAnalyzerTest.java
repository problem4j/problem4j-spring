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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.diagnostics.FailureAnalysis;

class DuplicateProblemResolverFailureAnalyzerTest {

  private static final String NL = System.lineSeparator();

  private final DuplicateProblemResolverFailureAnalyzer analyzer =
      new DuplicateProblemResolverFailureAnalyzer();

  @Test
  void givenWrappedDuplicateProblemResolverException_whenAnalyzing_thenConflictsAreDescribed() {
    Map<String, List<String>> conflicts = new LinkedHashMap<>();
    conflicts.put(
        "com.example.FirstException", List.of("'a' (com.example.A)", "'b' (com.example.B)"));
    conflicts.put(
        "com.example.SecondException", List.of("'c' (com.example.C)", "'d' (com.example.D)"));
    DuplicateProblemResolverException cause = new DuplicateProblemResolverException(conflicts);

    FailureAnalysis analysis =
        analyzer.analyze(new BeanCreationException("problemResolverStore", "failed", cause));

    assertThat(analysis)
        .isNotNull()
        .returns(
            "Multiple ProblemResolver beans with equal order handle the same exception class:"
                + NL
                + NL
                + "    com.example.FirstException:"
                + NL
                + "        - 'a' (com.example.A)"
                + NL
                + "        - 'b' (com.example.B)"
                + NL
                + "    com.example.SecondException:"
                + NL
                + "        - 'c' (com.example.C)"
                + NL
                + "        - 'd' (com.example.D)",
            FailureAnalysis::getDescription)
        .returns(DuplicateProblemResolverFailureAnalyzer.ACTION, FailureAnalysis::getAction)
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
  void givenConflicts_whenExceptionCreated_thenConflictsAreCopiedAndUnmodifiable() {
    Map<String, List<String>> conflicts = new LinkedHashMap<>();
    conflicts.put(
        "com.example.FirstException", List.of("'a' (com.example.A)", "'b' (com.example.B)"));

    DuplicateProblemResolverException exception = new DuplicateProblemResolverException(conflicts);
    conflicts.clear();

    assertThat(exception)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Multiple ProblemResolver beans with equal order for com.example.FirstException: 'a'"
                + " (com.example.A), 'b' (com.example.B). Use @Order or Ordered to set their"
                + " precedence.");
    assertThat(exception.getConflicts())
        .containsOnlyKeys("com.example.FirstException")
        .isUnmodifiable();
  }
}
