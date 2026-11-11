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

package io.github.problem4j.spring.webmvc.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.problem4j.spring.web.autoconfigure.UnavailableTracingSourceException;
import io.micrometer.tracing.handler.TracingObservationHandler;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.DefaultResourceLoader;

@ExtendWith(OutputCaptureExtension.class)
class UnavailableTracingSourceStartupWebMvcTest {

  @Test
  void givenMicrometerTracingSourceWithoutMicrometerTracing_whenStarting_thenFailureIsDescribed(
      CapturedOutput output) {
    SpringApplication application = new SpringApplication(TestApplication.class);
    application.setWebApplicationType(WebApplicationType.SERVLET);
    application.setResourceLoader(
        new DefaultResourceLoader(new FilteredClassLoader(TracingObservationHandler.class)));
    application.setDefaultProperties(
        Map.of("problem4j.tracing-source", "micrometer", "server.port", "0"));

    assertThatThrownBy(application::run)
        .hasRootCauseInstanceOf(UnavailableTracingSourceException.class);

    assertThat(output)
        .contains("APPLICATION FAILED TO START")
        .contains(
            "problem4j.tracing-source is set to micrometer, but io.micrometer:micrometer-tracing"
                + " is not on the classpath.")
        .contains("set problem4j.tracing-source to auto or simple.");
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApplication {}
}
