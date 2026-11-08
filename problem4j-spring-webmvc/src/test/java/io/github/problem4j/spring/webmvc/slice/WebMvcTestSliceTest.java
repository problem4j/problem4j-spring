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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.web.autoconfigure.ProblemAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;
import io.github.problem4j.spring.web.autoconfigure.ProblemWebAutoConfiguration;
import io.github.problem4j.spring.webmvc.autoconfigure.ProblemWebMvcAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(
    properties = {
      "problem4j.tracing-header-name=X-Trace-Id",
      "problem4j.instance-override=https://example.org/trace/{context.traceId}"
    })
class WebMvcTestSliceTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ApplicationContext context;

  @Test
  void givenWebMvcTestSlice_whenContextStarts_thenProblemAutoConfigurationsAreApplied() {
    assertThat(context.getBeansOfType(ProblemAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemWebAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemJacksonAutoConfiguration.class)).hasSize(1);
    assertThat(context.getBeansOfType(ProblemWebMvcAutoConfiguration.class)).hasSize(1);
  }

  @Test
  void givenProblemException_whenRequesting_thenProblemResponseIsReturned() throws Exception {
    mockMvc
        .perform(get("/slice/problem").header("X-Trace-Id", "slice-trace"))
        .andExpect(status().isConflict())
        .andExpect(header().string("Content-Type", Problem.CONTENT_TYPE_JSON))
        .andExpect(header().string("X-Trace-Id", "slice-trace"))
        .andExpect(
            content()
                .json(
                    """
                    {
                      "title": "Conflict",
                      "status": 409,
                      "detail": "slice conflict",
                      "instance": "https://example.org/trace/slice-trace"
                    }
                    """,
                    JsonCompareMode.STRICT));
  }

  @Test
  void givenUnknownPath_whenRequesting_thenProblemResponseIsReturned() throws Exception {
    mockMvc
        .perform(get("/slice/unknown"))
        .andExpect(status().isNotFound())
        .andExpect(header().string("Content-Type", Problem.CONTENT_TYPE_JSON));
  }

  @SpringBootConfiguration
  @Import(SliceController.class)
  static class SliceConfiguration {}

  @RestController
  static class SliceController {

    @GetMapping("/slice/problem")
    String problem() {
      throw new ProblemException(
          Problem.builder().title("Conflict").status(409).detail("slice conflict").build());
    }
  }
}
