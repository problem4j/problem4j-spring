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

package io.github.problem4j.spring.webmvc.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import io.github.problem4j.spring.webmvc.app.problem.ResolvableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootTest(
    classes = {
      WebMvcTestApp.class,
      InterceptorExceptionWebMvcTest.InterceptedController.class,
      InterceptorExceptionWebMvcTest.InterceptorConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.exception-handling.mode=cooperate",
      "problem4j.extension-overrides.processed=yes"
    })
@AutoConfigureTestRestTemplate
class InterceptorExceptionWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void givenExceptionOfHandlerInterceptor_whenRequesting_thenRenderedAsPostProcessedProblem() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/interceptor-exception", String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getHeaders().getContentType()).hasToString("application/problem+json");
    assertThat(response.getBody()).contains("\"processed\":\"yes\"");
  }

  @RestController
  static class InterceptedController {

    @GetMapping("/interceptor-exception")
    String intercepted() {
      return "never reached";
    }
  }

  @Configuration(proxyBeanMethods = false)
  static class InterceptorConfiguration {

    @Bean
    WebMvcConfigurer failingInterceptorConfigurer() {
      return new WebMvcConfigurer() {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
          registry
              .addInterceptor(
                  new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(
                        HttpServletRequest request, HttpServletResponse response, Object handler) {
                      throw new ResolvableException();
                    }
                  })
              .addPathPatterns("/interceptor-exception");
        }
      };
    }
  }
}
