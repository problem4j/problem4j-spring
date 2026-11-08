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

package io.github.problem4j.spring.web.slice;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemGsonAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.ApplicationContext;
import tools.jackson.databind.json.JsonMapper;

@JsonTest(properties = {"problem4j.enabled=false"})
class JsonTestSliceDisabledTest {

  @Autowired private ApplicationContext context;

  @Test
  void givenProblem4jDisabled_whenContextStarts_thenProblemAutoConfigurationsAreNotApplied() {
    assertThat(context.getBeansOfType(JsonMapper.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemAutoConfiguration.class)).isEmpty();
    assertThat(context.getBeansOfType(ProblemMapper.class)).isEmpty();
    assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).isEmpty();
    assertThat(context.getBeansOfType(ProblemGsonAutoConfiguration.class)).isEmpty();
  }

  @SpringBootConfiguration
  static class SliceConfiguration {}
}
