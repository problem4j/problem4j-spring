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

import io.github.problem4j.spring.webmvc.ExceptionWebMvcAdvice;
import io.github.problem4j.spring.webmvc.ProblemContextFilter;
import io.github.problem4j.spring.webmvc.ProblemContextMethodArgumentResolver;
import io.github.problem4j.spring.webmvc.ProblemExceptionWebMvcAdvice;
import io.github.problem4j.spring.webmvc.ProblemHandlerExceptionResolver;
import io.github.problem4j.spring.webmvc.ProblemResponseBodyAdvice;
import io.github.problem4j.spring.webmvc.ProblemResponseEntityExceptionHandler;
import io.github.problem4j.spring.webmvc.app.WebMvcTestApp;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

class ProblemWebMvcAutoConfigurationTest {

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.exception-handling.mode=advice"})
  @Nested
  class WithEnabled {

    @Autowired(required = false)
    private ProblemWebMvcAutoConfiguration problemWebMvcAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired(required = false)
    private ProblemResolverWebMvcConfiguration problemResolverWebMvcConfiguration;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoads() {
      assertThat(problemWebMvcAutoConfiguration).isNotNull();
      assertThat(problemErrorMvcConfiguration).isNotNull();
      assertThat(problemResolverWebMvcConfiguration).isNotNull();

      assertThat(properties.isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.enabled=false"})
  @Nested
  class WithParentDisabled {

    @Autowired(required = false)
    private ProblemWebMvcAutoConfiguration problemWebMvcAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired(required = false)
    private ProblemResolverWebMvcConfiguration problemResolverWebMvcConfiguration;

    @Autowired(required = false)
    private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutConfiguration() {
      assertThat(problemWebMvcAutoConfiguration).isNull();
      assertThat(problemErrorMvcConfiguration).isNull();
      assertThat(problemResolverWebMvcConfiguration).isNull();

      assertThat(properties).isNull();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.enabled=false"})
  @Nested
  class WithDisabled {

    @Autowired(required = false)
    private ProblemWebMvcAutoConfiguration problemWebMvcAutoConfiguration;

    @Autowired(required = false)
    private ProblemErrorMvcConfiguration problemErrorMvcConfiguration;

    @Autowired(required = false)
    private ProblemResolverWebMvcConfiguration problemResolverWebMvcConfiguration;

    @Autowired(required = false)
    private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutConfiguration() {
      assertThat(problemWebMvcAutoConfiguration).isNull();
      assertThat(problemErrorMvcConfiguration).isNull();
      assertThat(problemResolverWebMvcConfiguration).isNull();

      assertThat(properties).isNull();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.exception-handling.mode=advice"})
  @Nested
  class WithExceptionAdviceEnabled {

    @Autowired(required = false)
    private ExceptionWebMvcAdvice exceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionWebMvcAdvice).isNotNull();
      assertThat(properties.getExceptionAdvice().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {
        "problem4j.exception-handling.mode=advice",
        "problem4j.webmvc.exception-advice.enabled=false"
      })
  @Nested
  class WithExceptionAdviceDisabled {

    @Autowired(required = false)
    private ExceptionWebMvcAdvice exceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionWebMvcAdvice).isNull();
      assertThat(properties.getExceptionAdvice().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.exception-handling.mode=advice"})
  @Nested
  class WithProblemExceptionAdviceEnabled {

    @Autowired(required = false)
    private ProblemExceptionWebMvcAdvice problemExceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemExceptionWebMvcAdvice).isNotNull();
      assertThat(properties.getProblemExceptionAdvice().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {
        "problem4j.exception-handling.mode=advice",
        "problem4j.webmvc.problem-exception-advice.enabled=false"
      })
  @Nested
  class WithProblemExceptionAdviceDisabled {

    @Autowired(required = false)
    private ProblemExceptionWebMvcAdvice problemExceptionWebMvcAdvice;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemExceptionWebMvcAdvice).isNull();
      assertThat(properties.getProblemExceptionAdvice().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.exception-handling.mode=cooperate"})
  @Nested
  class WithCooperateMode {

    @Autowired(required = false)
    private ExceptionWebMvcAdvice exceptionWebMvcAdvice;

    @Autowired(required = false)
    private ProblemExceptionWebMvcAdvice problemExceptionWebMvcAdvice;

    @Autowired(required = false)
    private ResponseEntityExceptionHandler exceptionHandler;

    @Autowired(required = false)
    private ProblemResponseBodyAdvice problemResponseBodyAdvice;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Test
    void contextLoadsWithoutAdvicesAndWithCooperateHandling() {
      assertThat(exceptionWebMvcAdvice).isNull();
      assertThat(problemExceptionWebMvcAdvice).isNull();
      assertThat(exceptionHandler).isNull();
      assertThat(problemResponseBodyAdvice).isNotNull();
      assertThat(
              ((HandlerExceptionResolverComposite) handlerExceptionResolver)
                  .getExceptionResolvers())
          .hasAtLeastOneElementOfType(ProblemHandlerExceptionResolver.class);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.exception-handling.mode=cooperate"})
  @Nested
  class WithCooperateModeResolverConfigurer {

    @Autowired
    @Qualifier("problemExceptionResolverConfigurer")
    private WebMvcConfigurer configurer;

    @Test
    void givenExceptionHandlerExceptionResolver_whenExtending_thenResolverIsInsertedAfterIt() {
      List<HandlerExceptionResolver> resolvers = new ArrayList<>();
      resolvers.add(new ExceptionHandlerExceptionResolver());
      resolvers.add(new DefaultHandlerExceptionResolver());

      configurer.extendHandlerExceptionResolvers(resolvers);

      assertThat(resolvers).hasSize(3);
      assertThat(resolvers.get(1)).isInstanceOf(ProblemHandlerExceptionResolver.class);
    }

    @Test
    void givenNoExceptionHandlerExceptionResolver_whenExtending_thenResolverIsAppended() {
      List<HandlerExceptionResolver> resolvers = new ArrayList<>();
      resolvers.add(new DefaultHandlerExceptionResolver());

      configurer.extendHandlerExceptionResolvers(resolvers);

      assertThat(resolvers).hasSize(2);
      assertThat(resolvers.get(1))
          .isInstanceOfSatisfying(
              ProblemHandlerExceptionResolver.class,
              resolver -> assertThat(resolver.isUnknownExceptions()).isTrue());
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {
        "problem4j.exception-handling.mode=cooperate",
        "problem4j.webmvc.handler-exception-resolver.enabled=false",
        "problem4j.webmvc.response-body-advice.enabled=false"
      })
  @Nested
  class WithCooperateModeComponentsDisabled {

    @Autowired(required = false)
    private ProblemResponseBodyAdvice problemResponseBodyAdvice;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Test
    void contextLoadsWithoutCooperateHandling() {
      assertThat(problemResponseBodyAdvice).isNull();
      assertThat(
              ((HandlerExceptionResolverComposite) handlerExceptionResolver)
                  .getExceptionResolvers())
          .doesNotHaveAnyElementsOfTypes(ProblemHandlerExceptionResolver.class);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.exception-handling.mode=advice"})
  @Nested
  class WithAdviceMode {

    @Autowired(required = false)
    private ProblemResponseBodyAdvice problemResponseBodyAdvice;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Test
    void contextLoadsWithoutCooperateHandling() {
      assertThat(problemResponseBodyAdvice).isNull();
      assertThat(
              ((HandlerExceptionResolverComposite) handlerExceptionResolver)
                  .getExceptionResolvers())
          .doesNotHaveAnyElementsOfTypes(ProblemHandlerExceptionResolver.class);
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.exception-handling.mode=advice"})
  @Nested
  @SuppressWarnings("removal")
  class WithFormerResponseEntityExceptionHandlerType {

    @Autowired
    private io.github.problem4j.spring.webmvc.ProblemEnhancedWebMvcHandler
        responseEntityExceptionHandler;

    @Test
    void contextLoadsWithDefaultHandlerInjectableByFormerType() {
      assertThat(responseEntityExceptionHandler)
          .isInstanceOf(ProblemResponseEntityExceptionHandler.class);
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithProblemContextFilterEnabled {

    @Autowired(required = false)
    private ProblemContextFilter problemContextWebMvcFilter;

    @Autowired(required = false)
    private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemContextWebMvcFilter).isNotNull();
      assertThat(properties.getProblemContextFilter().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.webmvc.problem-context-filter.enabled=false"})
  @Nested
  class WithProblemContextFilterDisabled {

    @Autowired(required = false)
    private ProblemContextFilter problemContextWebMvcFilter;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(problemContextWebMvcFilter).isNull();
      assertThat(properties.getProblemContextFilter().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {"problem4j.exception-handling.mode=advice"})
  @Nested
  class WithExceptionHandlerEnabled {

    @Autowired(required = false)
    private ResponseEntityExceptionHandler exceptionHandler;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionHandler).isNotNull();
      assertThat(properties.getExceptionHandler().isEnabled()).isTrue();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class},
      properties = {
        "problem4j.exception-handling.mode=advice",
        "problem4j.webmvc.exception-handler.enabled=false"
      })
  @Nested
  class WithExceptionHandlerDisabled {

    @Autowired(required = false)
    private ResponseEntityExceptionHandler exceptionHandler;

    @Autowired private ProblemWebMvcProperties properties;

    @Test
    void contextLoadsWithoutExceptionAdvice() {
      assertThat(exceptionHandler).isNull();
      assertThat(properties.getExceptionHandler().isEnabled()).isFalse();
    }
  }

  @SpringBootTest(classes = {WebMvcTestApp.class})
  @Nested
  class WithDefaultProblemContextArgumentResolver {

    @Autowired private ProblemContextMethodArgumentResolver problemContextArgumentResolver;

    @Autowired
    @Qualifier("problemContextMethodArgumentResolverConfigurer")
    private WebMvcConfigurer problemContextMethodArgumentResolverConfigurer;

    @Test
    void contextLoadsWithDefaultResolver() {
      assertThat(problemContextArgumentResolver)
          .isExactlyInstanceOf(ProblemContextMethodArgumentResolver.class);
      assertThat(problemContextMethodArgumentResolverConfigurer).isNotNull();
    }
  }

  @SpringBootTest(
      classes = {WebMvcTestApp.class, CustomProblemContextArgumentResolverConfiguration.class})
  @Nested
  class WithCustomProblemContextArgumentResolver {

    @Autowired private ProblemContextMethodArgumentResolver problemContextArgumentResolver;

    @Test
    void contextLoadsWithCustomResolver() {
      assertThat(problemContextArgumentResolver)
          .isSameAs(CustomProblemContextArgumentResolverConfiguration.RESOLVER);
    }
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomProblemContextArgumentResolverConfiguration {

    static final ProblemContextMethodArgumentResolver RESOLVER =
        new ProblemContextMethodArgumentResolver();

    @Bean
    ProblemContextMethodArgumentResolver customProblemContextArgumentResolver() {
      return RESOLVER;
    }
  }
}
