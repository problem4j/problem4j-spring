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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * {@code HandlerExceptionResolver} beans of the application are sorted by {@code DispatcherServlet}
 * together with Spring's own composite resolver (order {@code 0}), which contains Problem4J one.
 */
@SpringBootTest(
    classes = {
      WebMvcTestApp.class,
      HandlerExceptionResolverOrderWebMvcTest.ResolvedController.class,
      HandlerExceptionResolverOrderWebMvcTest.ResolverConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "problem4j.exception-handling.mode=fallback",
      "problem4j.extension-overrides.processed=yes"
    })
@AutoConfigureTestRestTemplate
class HandlerExceptionResolverOrderWebMvcTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void givenOrderedResolverOfApplication_whenRequesting_thenItTakesPrecedence() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/resolver-order/ordered", String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(418);
  }

  @Test
  void givenUnorderedResolverOfApplication_whenRequesting_thenProblem4JTakesPrecedence() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/resolver-order/unordered", String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(500);
    assertThat(response.getHeaders().getContentType()).hasToString("application/problem+json");
    assertThat(response.getBody()).contains("\"processed\":\"yes\"");
  }

  static class OrderedResolverException extends RuntimeException {}

  static class UnorderedResolverException extends RuntimeException {}

  @RestController
  static class ResolvedController {

    @GetMapping("/resolver-order/ordered")
    String ordered() {
      throw new OrderedResolverException();
    }

    @GetMapping("/resolver-order/unordered")
    String unordered() {
      throw new UnorderedResolverException();
    }
  }

  @Configuration(proxyBeanMethods = false)
  static class ResolverConfiguration {

    @Bean
    HandlerExceptionResolver orderedApplicationResolver() {
      return new OrderedApplicationResolver();
    }

    /** Has no order of its own, so it is applied after Spring's composite resolver. */
    @Bean
    HandlerExceptionResolver unorderedApplicationResolver() {
      return (request, response, handler, ex) -> {
        if (ex instanceof UnorderedResolverException) {
          response.setStatus(418);
          return new ModelAndView();
        }
        return null;
      };
    }
  }

  /** Ordered before Spring's composite resolver, so it takes precedence over Problem4J. */
  static class OrderedApplicationResolver implements HandlerExceptionResolver, Ordered {

    @Override
    public @Nullable ModelAndView resolveException(
        HttpServletRequest request,
        HttpServletResponse response,
        @Nullable Object handler,
        Exception ex) {
      if (ex instanceof OrderedResolverException) {
        response.setStatus(418);
        return new ModelAndView();
      }
      return null;
    }

    @Override
    public int getOrder() {
      return Ordered.HIGHEST_PRECEDENCE;
    }
  }
}
