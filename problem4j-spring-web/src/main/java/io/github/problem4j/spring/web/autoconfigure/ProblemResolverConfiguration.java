/*
 * Copyright (c) 2025-2026 The Problem4J Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.problem4j.spring.web.autoconfigure;

import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
 * @see io.github.problem4j.spring.web.resolver.ProblemResolver
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.ANY)
@Configuration(proxyBeanMethods = false)
class ProblemResolverConfiguration {

  @ConditionalOnClass(BindException.class)
  @Configuration(proxyBeanMethods = false)
  static class BindProblemConfiguration {
    @ConditionalOnMissingBean(BindProblemResolver.class)
    @Bean
    BindProblemResolver bindProblemResolver(
        ProblemFormat problemFormat, BindingResultSupport bindingResultSupport) {
      return new BindProblemResolver(problemFormat, bindingResultSupport);
    }
  }

  @ConditionalOnClass(ConstraintViolationException.class)
  @Configuration(proxyBeanMethods = false)
  static class ConstraintViolationProblemConfiguration {
    @ConditionalOnMissingBean(ConstraintViolationProblemResolver.class)
    @Bean
    ConstraintViolationProblemResolver constraintViolationProblemResolver(
        ProblemFormat problemFormat) {
      return new ConstraintViolationProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(DecodingException.class)
  @Configuration(proxyBeanMethods = false)
  static class DecodingProblemConfiguration {
    @ConditionalOnMissingBean(DecodingProblemResolver.class)
    @Bean
    DecodingProblemResolver decodingProblemResolver(ProblemFormat problemFormat) {
      return new DecodingProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(ErrorResponseException.class)
  @Configuration(proxyBeanMethods = false)
  static class ErrorResponseProblemConfiguration {
    @ConditionalOnMissingBean(ErrorResponseProblemResolver.class)
    @Bean
    ErrorResponseProblemResolver errorResponseProblemResolver(ProblemFormat problemFormat) {
      return new ErrorResponseProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(HandlerMethodValidationException.class)
  @Configuration(proxyBeanMethods = false)
  static class HandlerMethodValidationProblemConfiguration {
    @ConditionalOnMissingBean(HandlerMethodValidationProblemResolver.class)
    @Bean
    HandlerMethodValidationProblemResolver handlerMethodValidationProblemResolver(
        ProblemFormat problemFormat, MethodValidationResultSupport methodValidationResultSupport) {
      return new HandlerMethodValidationProblemResolver(
          problemFormat, methodValidationResultSupport);
    }
  }

  @ConditionalOnClass(HttpMediaTypeNotAcceptableException.class)
  @Configuration(proxyBeanMethods = false)
  static class HttpMediaTypeNotAcceptableProblemConfiguration {
    @ConditionalOnMissingBean(HttpMediaTypeNotAcceptableProblemResolver.class)
    @Bean
    HttpMediaTypeNotAcceptableProblemResolver httpMediaTypeNotAcceptableProblemResolver(
        ProblemFormat problemFormat) {
      return new HttpMediaTypeNotAcceptableProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(HttpMediaTypeNotSupportedException.class)
  @Configuration(proxyBeanMethods = false)
  static class HttpMediaTypeNotSupportedProblemConfiguration {
    @ConditionalOnMissingBean(HttpMediaTypeNotSupportedProblemResolver.class)
    @Bean
    HttpMediaTypeNotSupportedProblemResolver httpMediaTypeNotSupportedProblemResolver(
        ProblemFormat problemFormat) {
      return new HttpMediaTypeNotSupportedProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(HttpMessageNotReadableException.class)
  @Configuration(proxyBeanMethods = false)
  static class HttpMessageNotReadableProblemConfiguration {
    @ConditionalOnMissingBean(HttpMessageNotReadableProblemResolver.class)
    @Bean
    HttpMessageNotReadableProblemResolver httpMessageNotReadableProblemResolver(
        ProblemFormat problemFormat, TypeNameMapper problemTypeNameMapper) {
      return new HttpMessageNotReadableProblemResolver(problemFormat, problemTypeNameMapper);
    }
  }

  @ConditionalOnClass(HttpRequestMethodNotSupportedException.class)
  @Configuration(proxyBeanMethods = false)
  static class HttpRequestMethodNotSupportedProblemConfiguration {
    @ConditionalOnMissingBean(HttpRequestMethodNotSupportedProblemResolver.class)
    @Bean
    HttpRequestMethodNotSupportedProblemResolver httpRequestMethodNotSupportedProblemResolver(
        ProblemFormat problemFormat) {
      return new HttpRequestMethodNotSupportedProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(MaxUploadSizeExceededException.class)
  @Configuration(proxyBeanMethods = false)
  static class MaxUploadSizeExceededProblemConfiguration {
    @ConditionalOnMissingBean(MaxUploadSizeExceededProblemResolver.class)
    @Bean
    MaxUploadSizeExceededProblemResolver maxUploadSizeExceededProblemResolver(
        ProblemFormat problemFormat) {
      return new MaxUploadSizeExceededProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(MethodValidationException.class)
  @Configuration(proxyBeanMethods = false)
  static class MethodValidationProblemConfiguration {
    @ConditionalOnMissingBean(MethodValidationProblemResolver.class)
    @Bean
    MethodValidationProblemResolver methodValidationProblemResolver(
        ProblemFormat problemFormat, MethodValidationResultSupport methodValidationResultSupport) {
      return new MethodValidationProblemResolver(problemFormat, methodValidationResultSupport);
    }
  }

  @ConditionalOnClass(MissingRequestValueException.class)
  @Configuration(proxyBeanMethods = false)
  static class MissingRequestValueProblemConfiguration {
    @ConditionalOnMissingBean(MissingRequestValueProblemResolver.class)
    @Bean
    MissingRequestValueProblemResolver missingRequestValueProblemResolver(
        ProblemFormat problemFormat) {
      return new MissingRequestValueProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(MissingServletRequestPartException.class)
  @Configuration(proxyBeanMethods = false)
  static class MissingServletRequestPartProblemConfiguration {
    @ConditionalOnMissingBean(MissingServletRequestPartProblemResolver.class)
    @Bean
    MissingServletRequestPartProblemResolver missingServletRequestPartProblemResolver(
        ProblemFormat problemFormat) {
      return new MissingServletRequestPartProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(MultipartException.class)
  @Configuration(proxyBeanMethods = false)
  static class MultipartProblemConfiguration {
    @ConditionalOnMissingBean(MultipartProblemResolver.class)
    @Bean
    MultipartProblemResolver multipartProblemResolver(ProblemFormat problemFormat) {
      return new MultipartProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(ResponseStatusException.class)
  @Configuration(proxyBeanMethods = false)
  static class ResponseStatusProblemConfiguration {
    @ConditionalOnMissingBean(ResponseStatusProblemResolver.class)
    @Bean
    ResponseStatusProblemResolver responseStatusProblemResolver(ProblemFormat problemFormat) {
      return new ResponseStatusProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(ServerErrorException.class)
  @Configuration(proxyBeanMethods = false)
  static class ServerErrorProblemConfiguration {
    @ConditionalOnMissingBean(ServerErrorProblemResolver.class)
    @Bean
    ServerErrorProblemResolver serverErrorProblemResolver(ProblemFormat problemFormat) {
      return new ServerErrorProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(ServerWebInputException.class)
  @Configuration(proxyBeanMethods = false)
  static class ServerWebInputProblemConfiguration {
    @ConditionalOnBean(TypeMismatchProblemResolver.class)
    @ConditionalOnMissingBean(ServerWebInputProblemResolver.class)
    @Bean
    ServerWebInputProblemResolver serverWebInputProblemResolver(
        ProblemFormat problemFormat,
        TypeMismatchProblemResolver typeMismatchProblemResolver,
        MethodParameterSupport methodParameterSupport,
        TypeNameMapper problemTypeNameMapper) {
      return new ServerWebInputProblemResolver(
          problemFormat,
          typeMismatchProblemResolver,
          methodParameterSupport,
          problemTypeNameMapper);
    }
  }

  @ConditionalOnClass(ServletRequestBindingException.class)
  @Configuration(proxyBeanMethods = false)
  static class ServletRequestBindingProblemConfiguration {
    @ConditionalOnMissingBean(ServletRequestBindingProblemResolver.class)
    @Bean
    ServletRequestBindingProblemResolver servletRequestBindingProblemResolver(
        ProblemFormat problemFormat) {
      return new ServletRequestBindingProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(TypeMismatchException.class)
  @Configuration(proxyBeanMethods = false)
  static class TypeMismatchProblemConfiguration {
    @ConditionalOnMissingBean(TypeMismatchProblemResolver.class)
    @Bean
    TypeMismatchProblemResolver typeMismatchProblemResolver(ProblemFormat problemFormat) {
      return new TypeMismatchProblemResolver(problemFormat);
    }
  }

  @ConditionalOnClass(WebExchangeBindException.class)
  @Configuration(proxyBeanMethods = false)
  static class WebExchangeBindProblemConfiguration {
    @ConditionalOnMissingBean(WebExchangeBindProblemResolver.class)
    @Bean
    WebExchangeBindProblemResolver webExchangeBindProblemResolver(
        ProblemFormat problemFormat, BindingResultSupport bindingResultSupport) {
      return new WebExchangeBindProblemResolver(problemFormat, bindingResultSupport);
    }
  }
}
