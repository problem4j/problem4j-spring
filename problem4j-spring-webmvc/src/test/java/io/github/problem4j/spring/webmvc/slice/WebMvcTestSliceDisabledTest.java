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

package io.github.problem4j.spring.webmvc.slice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.problem4j.core.Problem;
import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemWebAutoConfiguration;
import io.github.problem4j.spring.webmvc.autoconfigure.ProblemWebMvcAutoConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ContextConfiguration(classes = WebMvcTestSliceDisabledTest.SliceConfiguration.class)
class WebMvcTestSliceDisabledTest {

  private static void assertUnknownPathIsNotProblemResponse(MockMvc mockMvc) throws Exception {
    mockMvc
        .perform(get("/slice/unknown"))
        .andExpect(status().isNotFound())
        .andExpect(result ->
            assertThat(result.getResponse().getContentType())
                .isNotEqualTo(Problem.CONTENT_TYPE_JSON));
  }

  @Nested
  @TestPropertySource(properties = {"problem4j.enabled=false"})
  class Problem4jDisabled {

    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationContext context;

    @Test
    void givenProblem4jDisabled_whenContextStarts_thenProblemAutoConfigurationsAreNotApplied() {
      assertThat(context.getBeansOfType(ProblemAutoConfiguration.class)).isEmpty();
      assertThat(context.getBeansOfType(ProblemWebAutoConfiguration.class)).isEmpty();
      assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).isEmpty();
      assertThat(context.getBeansOfType(ProblemWebMvcAutoConfiguration.class)).isEmpty();
    }

    @Test
    void givenProblem4jDisabled_whenRequestingUnknownPath_thenProblemResponseIsNotReturned()
        throws Exception {
      assertUnknownPathIsNotProblemResponse(mockMvc);
    }
  }

  @Nested
  @TestPropertySource(properties = {"problem4j.webmvc.enabled=false"})
  class WebMvcDisabled {

    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationContext context;

    @Test
    void givenWebMvcDisabled_whenContextStarts_thenOnlyWebMvcAutoConfigurationIsNotApplied() {
      assertThat(context.getBeansOfType(ProblemAutoConfiguration.class)).hasSize(1);
      assertThat(context.getBeansOfType(ProblemWebAutoConfiguration.class)).hasSize(1);
      assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).hasSize(1);
      assertThat(context.getBeansOfType(ProblemWebMvcAutoConfiguration.class)).isEmpty();
    }

    @Test
    void givenWebMvcDisabled_whenRequestingUnknownPath_thenProblemResponseIsNotReturned()
        throws Exception {
      assertUnknownPathIsNotProblemResponse(mockMvc);
    }
  }

  @SpringBootConfiguration
  static class SliceConfiguration {}
}
