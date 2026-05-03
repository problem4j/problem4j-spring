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

package io.github.problem4j.spring.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.util.StringUtils;

class DefaultProblemPostProcessorTest {

  @Test
  void shouldReturnSameProblemWhenNoOverrides() {
    PostProcessorSettings settings = getSettings(null, null);

    ProblemContext context = ProblemContext.create().put("traceId", "trace-123");
    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenEmptyTypeOverride_shouldNotChangeProblemType() {
    PostProcessorSettings settings = getSettings("", null);

    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();
    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);

    Problem result = processor.process(null, problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenEmptyInstanceOverride_shouldNotChangeProblemInstance() {
    PostProcessorSettings settings = getSettings(null, "");

    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();
    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);

    Problem result = processor.process(null, problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenStaticTypeOverride_shouldOverrideType() {
    PostProcessorSettings settings = getSettings("/errors/static", null);

    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(null, problem);

    assertThat(result).isEqualTo(problem.toBuilder().type("/errors/static").build());
  }

  @Test
  void givenStaticInstanceOverride_shouldOverrideInstance() {
    PostProcessorSettings settings = getSettings(null, "/instances/static");

    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(null, problem);

    assertThat(result).isEqualTo(problem.toBuilder().instance("/instances/static").build());
  }

  @Test
  void givenProblemTypePlaceholderWithTypeValue_shouldOverrideType() {
    PostProcessorSettings settings = getSettings("/errors/{problem.type}", null);

    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(null, problem);

    assertThat(result).isEqualTo(problem.toBuilder().type("/errors/bad_request").build());
  }

  @Test
  void givenProblemTypePlaceholderWithEmptyTypeValue_shouldNotOverrideType() {
    PostProcessorSettings settings = getSettings("/errors/{problem.type}", null);

    Problem problem = Problem.builder().type("").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(null, problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenContextTraceIdPlaceholderWithTraceIdValue_shouldOverrideInstance() {
    PostProcessorSettings settings = getSettings(null, "trace:{context.traceId}");

    ProblemContext context = ProblemContext.create().put("traceId", "trace-abc");
    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    assertThat(result).isEqualTo(problem.toBuilder().instance("trace:trace-abc").build());
  }

  @Test
  void givenContextTraceIdPlaceholderWithoutTraceIdValue_shouldNotOverrideInstance() {
    PostProcessorSettings settings = getSettings(null, "trace:{context.traceId}");

    ProblemContext context = ProblemContext.create();
    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenContextTraceIdPlaceholderWithEmptyTraceIdValue_shouldNotOverrideInstance() {
    PostProcessorSettings settings = getSettings(null, "trace:{context.traceId}");

    ProblemContext context = ProblemContext.create().put("traceId", "");
    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenProblemInstancePlaceholderWithValue_shouldOverrideInstance() {
    PostProcessorSettings settings = getSettings(null, "original:{problem.instance}");

    Problem problem = Problem.builder().type("bad_request").instance("inst-777").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(null, problem);

    assertThat(result).isEqualTo(problem.toBuilder().instance("original:inst-777").build());
  }

  @Test
  void givenProblemInstancePlaceholderWithoutValue_shouldNotOverrideInstance() {
    PostProcessorSettings settings = getSettings(null, "original:{problem.instance}");

    Problem problem = Problem.builder().type("bad_request").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(null, problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenProblemInstancePlaceholderWithEmptyValue_shouldNotOverrideInstance() {
    PostProcessorSettings settings = getSettings(null, "original:{problem.instance}");

    Problem problem = Problem.builder().type("bad_request").instance("").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(null, problem);

    assertThat(result).isSameAs(problem);
  }

  @ParameterizedTest(name = "{index} => problemInstance={0}, contextTraceId={1}, expected={2}")
  @CsvSource(
      value = {
        "'inst', 'trace-X', 'trace-X/inst'",
        "'inst', '',        ''",
        "'inst', null,      ''",
        "'',     'trace-Y', ''",
        "null,   'trace-Y', ''"
      },
      nullValues = "null")
  void givenProblemInstanceAndContextTraceIdPlaceholders_shouldNotOverrideIfAnyIsMissing(
      String problemInstance, String contextTraceId, String expectedResult) {
    PostProcessorSettings settings = getSettings(null, "{context.traceId}/{problem.instance}");

    ProblemBuilder builder = Problem.builder().type("bad_request");
    if (problemInstance != null) {
      builder = builder.instance(problemInstance);
    }
    Problem problem = builder.build();

    ProblemContext context = ProblemContext.create().put("traceId", contextTraceId);

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    if (StringUtils.hasLength(expectedResult)) {
      assertThat(result).isEqualTo(problem.toBuilder().instance(expectedResult).build());
    } else {
      assertThat(result).isSameAs(problem);
    }
  }

  @Test
  void givenOverrideWithUnknownPlaceholders_shouldRemoveUnknownPlaceholders() {
    PostProcessorSettings settings = getSettings("type-{unknown}", "instance-{unknown}");
    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);

    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();
    Problem result = processor.process(ProblemContext.create().put("traceId", "x"), problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenContextPlaceholderInTypeOverride_withValue_shouldOverrideType() {
    PostProcessorSettings settings = getSettings("/errors/{context.errorCode}", null, null);

    ProblemContext context = ProblemContext.create().put("errorCode", "ERR-42");
    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    assertThat(result).isEqualTo(problem.toBuilder().type("/errors/ERR-42").build());
  }

  @Test
  void givenContextPlaceholderInTypeOverride_withoutValue_shouldNotOverrideType() {
    PostProcessorSettings settings = getSettings("/errors/{context.errorCode}", null, null);

    ProblemContext context = ProblemContext.create();
    Problem problem = Problem.builder().type("bad_request").instance("instance-1").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenStaticTitleOverride_shouldOverrideTitle() {
    PostProcessorSettings settings = getSettings(null, "Custom Error", null);

    Problem problem = Problem.builder().type("bad_request").title("Bad Request").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(null, problem);

    assertThat(result).isEqualTo(problem.toBuilder().title("Custom Error").build());
  }

  @Test
  void givenProblemTitlePlaceholderWithTitleValue_shouldOverrideTitle() {
    PostProcessorSettings settings = getSettings(null, "Prefix: {problem.title}", null);

    Problem problem = Problem.builder().type("bad_request").title("Bad Request").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(null, problem);

    assertThat(result).isEqualTo(problem.toBuilder().title("Prefix: Bad Request").build());
  }

  @Test
  void givenContextPlaceholderInTitleOverride_withValue_shouldOverrideTitle() {
    PostProcessorSettings settings = getSettings(null, "{context.env} Error", null);

    ProblemContext context = ProblemContext.create().put("env", "prod");
    Problem problem = Problem.builder().type("bad_request").title("Bad Request").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    assertThat(result).isEqualTo(problem.toBuilder().title("prod Error").build());
  }

  @Test
  void givenContextPlaceholderInTitleOverride_withoutValue_shouldNotOverrideTitle() {
    PostProcessorSettings settings = getSettings(null, "{context.env} Error", null);

    ProblemContext context = ProblemContext.create();
    Problem problem = Problem.builder().type("bad_request").title("Bad Request").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    assertThat(result).isSameAs(problem);
  }

  @Test
  void givenContextPlaceholderInInstanceOverride_withNonTraceIdKey_shouldOverrideInstance() {
    PostProcessorSettings settings = getSettings(null, null, "/req/{context.requestId}");

    ProblemContext context = ProblemContext.create().put("requestId", "req-99");
    Problem problem = Problem.builder().type("bad_request").instance("old-instance").build();

    ProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);
    Problem result = processor.process(context, problem);

    assertThat(result).isEqualTo(problem.toBuilder().instance("/req/req-99").build());
  }

  @Test
  void givenProcessorWithSettings_whenGetSettings_thenReturnsSameInstance() {
    PostProcessorSettings settings =
        getSettings("/types/{problem.type}", null, "/instances/{context.traceId}");
    DefaultProblemPostProcessor processor = new DefaultProblemPostProcessor(settings);

    assertThat(processor.getSettings()).isSameAs(settings);
  }

  private PostProcessorSettings getSettings(
      @Nullable String typeOverride, @Nullable String instanceOverride) {
    return getSettings(typeOverride, null, instanceOverride);
  }

  private PostProcessorSettings getSettings(
      @Nullable String typeOverride,
      @Nullable String titleOverride,
      @Nullable String instanceOverride) {
    return new PostProcessorSettings() {
      @Override
      public @Nullable String getTypeOverride() {
        return typeOverride;
      }

      @Override
      public @Nullable String getTitleOverride() {
        return titleOverride;
      }

      @Override
      public @Nullable String getInstanceOverride() {
        return instanceOverride;
      }
    };
  }
}
