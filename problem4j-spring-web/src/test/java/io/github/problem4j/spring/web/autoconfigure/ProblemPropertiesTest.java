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

import io.github.problem4j.spring.web.autoconfigure.ProblemProperties.DetailFormat;
import io.github.problem4j.spring.web.autoconfigure.ProblemProperties.ResolverCaching;
import org.junit.jupiter.api.Test;

// These tests exist only to play with getters/setters and the deprecated constructors so they are
// not reported as uncovered while playing with JaCoCo test coverage.
@SuppressWarnings("removal")
class ProblemPropertiesTest {

  @Test
  void givenNoArgConstructor_whenCreated_thenDefaultsApplied() {
    ProblemProperties properties = new ProblemProperties();

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getDetailFormat()).isEqualTo(DetailFormat.CAPITALIZED);
    assertThat(properties.getTracingHeaderName()).isNull();
    assertThat(properties.getTypeOverride()).isNull();
    assertThat(properties.getTitleOverride()).isNull();
    assertThat(properties.getInstanceOverride()).isNull();
    assertThat(properties.getResolverCaching()).isNotNull();
    assertThat(properties.getResolverCaching().isEnabled())
        .isEqualTo(ResolverCaching.DEFAULT_ENABLED);
  }

  @Test
  void givenSetters_whenInvoked_thenValuesRoundTrip() {
    ProblemProperties properties = new ProblemProperties();
    ResolverCaching caching = new ResolverCaching();
    caching.setEnabled(true);

    properties.setEnabled(false);
    properties.setDetailFormat(DetailFormat.UPPERCASE);
    properties.setTracingHeaderName("X-Trace-Id");
    properties.setTypeOverride("type");
    properties.setTitleOverride("title");
    properties.setInstanceOverride("instance");
    properties.setResolverCaching(caching);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getDetailFormat()).isEqualTo(DetailFormat.UPPERCASE);
    assertThat(properties.getTracingHeaderName()).isEqualTo("X-Trace-Id");
    assertThat(properties.getTypeOverride()).isEqualTo("type");
    assertThat(properties.getTitleOverride()).isEqualTo("title");
    assertThat(properties.getInstanceOverride()).isEqualTo("instance");
    assertThat(properties.getResolverCaching()).isSameAs(caching);
    assertThat(properties.getResolverCaching().isEnabled()).isTrue();
  }

  @Test
  void givenDeprecatedConstructor_whenAllArgsProvided_thenValuesApplied() {
    ResolverCaching caching = new ResolverCaching(true);

    ProblemProperties properties =
        new ProblemProperties(
            false, DetailFormat.LOWERCASE, "X-Trace-Id", "t", "ti", "in", caching);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getDetailFormat()).isEqualTo(DetailFormat.LOWERCASE);
    assertThat(properties.getTracingHeaderName()).isEqualTo("X-Trace-Id");
    assertThat(properties.getTypeOverride()).isEqualTo("t");
    assertThat(properties.getTitleOverride()).isEqualTo("ti");
    assertThat(properties.getInstanceOverride()).isEqualTo("in");
    assertThat(properties.getResolverCaching()).isSameAs(caching);
    assertThat(properties.getResolverCaching().isEnabled()).isTrue();
  }

  @Test
  void givenDeprecatedConstructor_whenResolverCachingNull_thenDefaultRetained() {
    ProblemProperties properties =
        new ProblemProperties(true, DetailFormat.CAPITALIZED, null, null, null, null, null);

    assertThat(properties.getResolverCaching()).isNotNull();
    assertThat(properties.getResolverCaching().isEnabled())
        .isEqualTo(ResolverCaching.DEFAULT_ENABLED);
  }

  @Test
  void givenResolverCachingNoArgConstructor_whenCreated_thenDefaultEnabled() {
    ResolverCaching caching = new ResolverCaching();

    assertThat(caching.isEnabled()).isEqualTo(ResolverCaching.DEFAULT_ENABLED);
  }

  @Test
  void givenResolverCachingSetter_whenInvoked_thenValueRoundTrips() {
    ResolverCaching caching = new ResolverCaching();

    caching.setEnabled(true);

    assertThat(caching.isEnabled()).isTrue();
  }

  @Test
  void givenResolverCachingDeprecatedConstructor_whenCreated_thenValueApplied() {
    ResolverCaching caching = new ResolverCaching(true);

    assertThat(caching.isEnabled()).isTrue();
  }

  @Test
  void givenDetailFormatConstants_whenRead_thenExpectedTokens() {
    assertThat(DetailFormat.LOWERCASE).isEqualTo("lowercase");
    assertThat(DetailFormat.CAPITALIZED).isEqualTo("capitalized");
    assertThat(DetailFormat.UPPERCASE).isEqualTo("uppercase");
  }
}
