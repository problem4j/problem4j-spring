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

import io.github.problem4j.spring.web.resolver.BindProblemResolver;
import io.github.problem4j.spring.web.resolver.ConstraintViolationProblemResolver;
import io.github.problem4j.spring.web.resolver.DecodingProblemResolver;
import io.github.problem4j.spring.web.resolver.ErrorResponseProblemResolver;
import io.github.problem4j.spring.web.resolver.HandlerMethodValidationProblemResolver;
import io.github.problem4j.spring.web.resolver.HttpMediaTypeNotAcceptableProblemResolver;
import io.github.problem4j.spring.web.resolver.HttpMediaTypeNotSupportedProblemResolver;
import io.github.problem4j.spring.web.resolver.HttpMessageNotReadableProblemResolver;
import io.github.problem4j.spring.web.resolver.HttpRequestMethodNotSupportedProblemResolver;
import io.github.problem4j.spring.web.resolver.MaxUploadSizeExceededProblemResolver;
import io.github.problem4j.spring.web.resolver.MethodValidationProblemResolver;
import io.github.problem4j.spring.web.resolver.MissingRequestValueProblemResolver;
import io.github.problem4j.spring.web.resolver.MissingServletRequestPartProblemResolver;
import io.github.problem4j.spring.web.resolver.MultipartProblemResolver;
import io.github.problem4j.spring.web.resolver.ResponseStatusProblemResolver;
import io.github.problem4j.spring.web.resolver.ServerErrorProblemResolver;
import io.github.problem4j.spring.web.resolver.ServerWebInputProblemResolver;
import io.github.problem4j.spring.web.resolver.ServletRequestBindingProblemResolver;
import io.github.problem4j.spring.web.resolver.TypeMismatchProblemResolver;
import io.github.problem4j.spring.web.resolver.WebExchangeBindProblemResolver;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;

/**
 * Spring configuration for registering {@code ProblemResolver} beans for {@code spring-web}
 * library. Modules {@code problem4j-spring-webflux} and {@code problem4j-spring-webmvc} provide
 * additional {@link Configuration} classes with more resolvers, that originate from other Spring
 * libraries.
 *
 * <p>Each nested configuration class is annotated with {@link ConditionalOnClass} to ensure that
 * only resolvers for classes present on the classpath are created. This design allows the library
 * to remain compatible previous versions.
 *
 * <p>Every resolver is constructed with its default (no-arg) constructor here; any dependency it
 * needs ({@code ProblemFormat}, {@code TypeNameMapper}, {@code BindingResultSupport}, {@code
 * MethodValidationResultSupport}, {@code MethodParameterSupport}, {@code
 * TypeMismatchProblemResolver}) is injected after construction by the corresponding {@code
 * *AwareBeanPostProcessor}.
 *
 * @see io.github.problem4j.spring.web.resolver.ProblemResolver
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.ANY)
@Configuration(proxyBeanMethods = false)
class ProblemResolverConfiguration {

  /** Creates a new instance of this configuration. */
  ProblemResolverConfiguration() {}

  @ConditionalOnClass(BindException.class)
  @Configuration(proxyBeanMethods = false)
  static class BindProblemConfiguration {

    /** Creates a new instance of this configuration. */
    BindProblemConfiguration() {}

    @ConditionalOnMissingBean(BindProblemResolver.class)
    @Bean
    BindProblemResolver bindProblemResolver() {
      return new BindProblemResolver();
    }
  }

  @ConditionalOnClass(ConstraintViolationException.class)
  @Configuration(proxyBeanMethods = false)
  static class ConstraintViolationProblemConfiguration {

    /** Creates a new instance of this configuration. */
    ConstraintViolationProblemConfiguration() {}

    @ConditionalOnMissingBean(ConstraintViolationProblemResolver.class)
    @Bean
    ConstraintViolationProblemResolver constraintViolationProblemResolver() {
      return new ConstraintViolationProblemResolver();
    }
  }

  @ConditionalOnClass(DecodingException.class)
  @Configuration(proxyBeanMethods = false)
  static class DecodingProblemConfiguration {

    /** Creates a new instance of this configuration. */
    DecodingProblemConfiguration() {}

    @ConditionalOnMissingBean(DecodingProblemResolver.class)
    @Bean
    DecodingProblemResolver decodingProblemResolver() {
      return new DecodingProblemResolver();
    }
  }

  @ConditionalOnClass(ErrorResponseException.class)
  @Configuration(proxyBeanMethods = false)
  static class ErrorResponseProblemConfiguration {

    /** Creates a new instance of this configuration. */
    ErrorResponseProblemConfiguration() {}

    @ConditionalOnMissingBean(ErrorResponseProblemResolver.class)
    @Bean
    ErrorResponseProblemResolver errorResponseProblemResolver() {
      return new ErrorResponseProblemResolver();
    }
  }

  @ConditionalOnClass(HandlerMethodValidationException.class)
  @Configuration(proxyBeanMethods = false)
  static class HandlerMethodValidationProblemConfiguration {

    /** Creates a new instance of this configuration. */
    HandlerMethodValidationProblemConfiguration() {}

    @ConditionalOnMissingBean(HandlerMethodValidationProblemResolver.class)
    @Bean
    HandlerMethodValidationProblemResolver handlerMethodValidationProblemResolver() {
      return new HandlerMethodValidationProblemResolver();
    }
  }

  @ConditionalOnClass(HttpMediaTypeNotAcceptableException.class)
  @Configuration(proxyBeanMethods = false)
  static class HttpMediaTypeNotAcceptableProblemConfiguration {

    /** Creates a new instance of this configuration. */
    HttpMediaTypeNotAcceptableProblemConfiguration() {}

    @ConditionalOnMissingBean(HttpMediaTypeNotAcceptableProblemResolver.class)
    @Bean
    HttpMediaTypeNotAcceptableProblemResolver httpMediaTypeNotAcceptableProblemResolver() {
      return new HttpMediaTypeNotAcceptableProblemResolver();
    }
  }

  @ConditionalOnClass(HttpMediaTypeNotSupportedException.class)
  @Configuration(proxyBeanMethods = false)
  static class HttpMediaTypeNotSupportedProblemConfiguration {

    /** Creates a new instance of this configuration. */
    HttpMediaTypeNotSupportedProblemConfiguration() {}

    @ConditionalOnMissingBean(HttpMediaTypeNotSupportedProblemResolver.class)
    @Bean
    HttpMediaTypeNotSupportedProblemResolver httpMediaTypeNotSupportedProblemResolver() {
      return new HttpMediaTypeNotSupportedProblemResolver();
    }
  }

  @ConditionalOnClass(HttpMessageNotReadableException.class)
  @Configuration(proxyBeanMethods = false)
  static class HttpMessageNotReadableProblemConfiguration {

    /** Creates a new instance of this configuration. */
    HttpMessageNotReadableProblemConfiguration() {}

    @ConditionalOnMissingBean(HttpMessageNotReadableProblemResolver.class)
    @Bean
    HttpMessageNotReadableProblemResolver httpMessageNotReadableProblemResolver() {
      return new HttpMessageNotReadableProblemResolver();
    }
  }

  @ConditionalOnClass(HttpRequestMethodNotSupportedException.class)
  @Configuration(proxyBeanMethods = false)
  static class HttpRequestMethodNotSupportedProblemConfiguration {

    /** Creates a new instance of this configuration. */
    HttpRequestMethodNotSupportedProblemConfiguration() {}

    @ConditionalOnMissingBean(HttpRequestMethodNotSupportedProblemResolver.class)
    @Bean
    HttpRequestMethodNotSupportedProblemResolver httpRequestMethodNotSupportedProblemResolver() {
      return new HttpRequestMethodNotSupportedProblemResolver();
    }
  }

  @ConditionalOnClass(MaxUploadSizeExceededException.class)
  @Configuration(proxyBeanMethods = false)
  static class MaxUploadSizeExceededProblemConfiguration {

    /** Creates a new instance of this configuration. */
    MaxUploadSizeExceededProblemConfiguration() {}

    @ConditionalOnMissingBean(MaxUploadSizeExceededProblemResolver.class)
    @Bean
    MaxUploadSizeExceededProblemResolver maxUploadSizeExceededProblemResolver() {
      return new MaxUploadSizeExceededProblemResolver();
    }
  }

  @ConditionalOnClass(MethodValidationException.class)
  @Configuration(proxyBeanMethods = false)
  static class MethodValidationProblemConfiguration {

    /** Creates a new instance of this configuration. */
    MethodValidationProblemConfiguration() {}

    @ConditionalOnMissingBean(MethodValidationProblemResolver.class)
    @Bean
    MethodValidationProblemResolver methodValidationProblemResolver() {
      return new MethodValidationProblemResolver();
    }
  }

  @ConditionalOnClass(MissingRequestValueException.class)
  @Configuration(proxyBeanMethods = false)
  static class MissingRequestValueProblemConfiguration {

    /** Creates a new instance of this configuration. */
    MissingRequestValueProblemConfiguration() {}

    @ConditionalOnMissingBean(MissingRequestValueProblemResolver.class)
    @Bean
    MissingRequestValueProblemResolver missingRequestValueProblemResolver() {
      return new MissingRequestValueProblemResolver();
    }
  }

  @ConditionalOnClass(MissingServletRequestPartException.class)
  @Configuration(proxyBeanMethods = false)
  static class MissingServletRequestPartProblemConfiguration {

    /** Creates a new instance of this configuration. */
    MissingServletRequestPartProblemConfiguration() {}

    @ConditionalOnMissingBean(MissingServletRequestPartProblemResolver.class)
    @Bean
    MissingServletRequestPartProblemResolver missingServletRequestPartProblemResolver() {
      return new MissingServletRequestPartProblemResolver();
    }
  }

  @ConditionalOnClass(MultipartException.class)
  @Configuration(proxyBeanMethods = false)
  static class MultipartProblemConfiguration {

    /** Creates a new instance of this configuration. */
    MultipartProblemConfiguration() {}

    @ConditionalOnMissingBean(MultipartProblemResolver.class)
    @Bean
    MultipartProblemResolver multipartProblemResolver() {
      return new MultipartProblemResolver();
    }
  }

  @ConditionalOnClass(ResponseStatusException.class)
  @Configuration(proxyBeanMethods = false)
  static class ResponseStatusProblemConfiguration {

    /** Creates a new instance of this configuration. */
    ResponseStatusProblemConfiguration() {}

    @ConditionalOnMissingBean(ResponseStatusProblemResolver.class)
    @Bean
    ResponseStatusProblemResolver responseStatusProblemResolver() {
      return new ResponseStatusProblemResolver();
    }
  }

  @ConditionalOnClass(ServerErrorException.class)
  @Configuration(proxyBeanMethods = false)
  static class ServerErrorProblemConfiguration {

    /** Creates a new instance of this configuration. */
    ServerErrorProblemConfiguration() {}

    @ConditionalOnMissingBean(ServerErrorProblemResolver.class)
    @Bean
    ServerErrorProblemResolver serverErrorProblemResolver() {
      return new ServerErrorProblemResolver();
    }
  }

  @ConditionalOnClass(ServerWebInputException.class)
  @Configuration(proxyBeanMethods = false)
  static class ServerWebInputProblemConfiguration {

    /** Creates a new instance of this configuration. */
    ServerWebInputProblemConfiguration() {}

    @ConditionalOnMissingBean(ServerWebInputProblemResolver.class)
    @Bean
    ServerWebInputProblemResolver serverWebInputProblemResolver() {
      return new ServerWebInputProblemResolver();
    }
  }

  @ConditionalOnClass(ServletRequestBindingException.class)
  @Configuration(proxyBeanMethods = false)
  static class ServletRequestBindingProblemConfiguration {

    /** Creates a new instance of this configuration. */
    ServletRequestBindingProblemConfiguration() {}

    @ConditionalOnMissingBean(ServletRequestBindingProblemResolver.class)
    @Bean
    ServletRequestBindingProblemResolver servletRequestBindingProblemResolver() {
      return new ServletRequestBindingProblemResolver();
    }
  }

  @ConditionalOnClass(TypeMismatchException.class)
  @Configuration(proxyBeanMethods = false)
  static class TypeMismatchProblemConfiguration {

    /** Creates a new instance of this configuration. */
    TypeMismatchProblemConfiguration() {}

    @ConditionalOnMissingBean(TypeMismatchProblemResolver.class)
    @Bean
    TypeMismatchProblemResolver typeMismatchProblemResolver() {
      return new TypeMismatchProblemResolver();
    }
  }

  @ConditionalOnClass(WebExchangeBindException.class)
  @Configuration(proxyBeanMethods = false)
  static class WebExchangeBindProblemConfiguration {

    /** Creates a new instance of this configuration. */
    WebExchangeBindProblemConfiguration() {}

    @ConditionalOnMissingBean(WebExchangeBindProblemResolver.class)
    @Bean
    WebExchangeBindProblemResolver webExchangeBindProblemResolver() {
      return new WebExchangeBindProblemResolver();
    }
  }
}
