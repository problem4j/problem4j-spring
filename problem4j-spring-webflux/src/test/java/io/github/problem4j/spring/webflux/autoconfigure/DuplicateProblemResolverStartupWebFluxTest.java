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

package io.github.problem4j.spring.webflux.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.autoconfigure.DuplicateProblemResolverException;
import io.github.problem4j.spring.web.resolver.BindProblemResolver;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindException;

@ExtendWith(OutputCaptureExtension.class)
class DuplicateProblemResolverStartupWebFluxTest {

  @Test
  void givenCustomResolverForBuiltInExceptionClass_whenStarting_thenConflictIsDescribed(
      CapturedOutput output) {
    SpringApplication application = new SpringApplication(TestApplication.class);
    application.setWebApplicationType(WebApplicationType.REACTIVE);
    application.setDefaultProperties(Map.of("server.port", "0"));

    assertThatThrownBy(application::run)
        .hasRootCauseInstanceOf(DuplicateProblemResolverException.class);

    assertThat(output)
        .contains("APPLICATION FAILED TO START")
        .contains(
            "Multiple ProblemResolver beans with equal order handle the same exception class:")
        .contains("    " + BindException.class.getName() + ":")
        .contains("        - 'bindProblemResolver' (" + BindProblemResolver.class.getName() + ")")
        .contains(
            "        - 'customBindProblemResolver' ("
                + CustomBindProblemResolver.class.getName()
                + ")")
        .contains("set problem4j.duplicate-resolver-policy to first or last");
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApplication {

    @Bean
    ProblemResolver customBindProblemResolver() {
      return new CustomBindProblemResolver();
    }
  }

  static class CustomBindProblemResolver implements ProblemResolver {

    @Override
    public Class<? extends Exception> getExceptionClass() {
      return BindException.class;
    }

    @Override
    public Problem resolve(
        ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
      return Problem.builder().status(400).build();
    }
  }
}
