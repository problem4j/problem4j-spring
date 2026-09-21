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

package io.github.problem4j.spring.web.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemHandler;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

class ProblemHandlerMethodProcessorTest {

  @Test
  void givenHandlerWithAllParameters_whenResolving_thenPassesAllArguments() {
    DefaultListableBeanFactory beanFactory = process(AllParametersHandlers.class);
    HttpHeaders headers = new HttpHeaders();

    Problem problem =
        resolverFor(beanFactory, OutOfStockException.class)
            .resolve(
                ProblemContext.create().put("traceId", "trace-1"),
                new OutOfStockException("SKU-1"),
                headers,
                HttpStatus.CONFLICT);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(409)
                .extension("sku", "SKU-1")
                .extension("traceId", "trace-1")
                .build());
    assertThat(headers.getFirst("X-Resolved-By")).isEqualTo("allParameters");
  }

  @Test
  void givenHandlerWithOnlyException_whenResolving_thenResolvesProblem() {
    DefaultListableBeanFactory beanFactory = process(SelectedParametersHandlers.class);

    Problem problem = resolve(beanFactory, new PaymentDeclinedException());

    assertThat(problem).isEqualTo(Problem.builder().status(402).build());
  }

  @Test
  void givenHandlerWithExceptionAndContext_whenResolving_thenPassesSelectedArguments() {
    DefaultListableBeanFactory beanFactory = process(SelectedParametersHandlers.class);

    Problem problem =
        resolverFor(beanFactory, OutOfStockException.class)
            .resolve(
                ProblemContext.create().put("traceId", "trace-2"),
                new OutOfStockException("SKU-2"),
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(409)
                .extension("sku", "SKU-2")
                .extension("traceId", "trace-2")
                .build());
  }

  @Test
  void givenHandlerWithStatusOnly_whenResolving_thenPassesStatus() {
    DefaultListableBeanFactory beanFactory = process(StatusOnlyHandlers.class);

    Problem problem =
        resolverFor(beanFactory, PaymentDeclinedException.class)
            .resolve(
                ProblemContext.create(),
                new PaymentDeclinedException(),
                new HttpHeaders(),
                HttpStatus.SERVICE_UNAVAILABLE);

    assertThat(problem).isEqualTo(Problem.builder().status(503).build());
  }

  @Test
  void givenHandlerWithParametersInDifferentOrder_whenResolving_thenMatchesArgumentsByType() {
    DefaultListableBeanFactory beanFactory = process(ReorderedParametersHandlers.class);
    HttpHeaders headers = new HttpHeaders();

    Problem problem =
        resolverFor(beanFactory, OutOfStockException.class)
            .resolve(
                ProblemContext.create().put("traceId", "trace-3"),
                new OutOfStockException("SKU-3"),
                headers,
                HttpStatus.CONFLICT);

    assertThat(problem)
        .isEqualTo(
            Problem.builder()
                .status(409)
                .extension("sku", "SKU-3")
                .extension("traceId", "trace-3")
                .build());
    assertThat(headers.getFirst("X-Resolved-By")).isEqualTo("reordered");
  }

  @Test
  void givenPublicHandler_whenResolving_thenInvokesMethod() {
    DefaultListableBeanFactory beanFactory = process(PublicHandlers.class);

    Problem problem = resolve(beanFactory, new PaymentDeclinedException());

    assertThat(problem).isEqualTo(Problem.builder().status(402).build());
  }

  @Test
  void givenPackagePrivateHandler_whenResolving_thenInvokesMethod() {
    DefaultListableBeanFactory beanFactory = process(SelectedParametersHandlers.class);

    Problem problem = resolve(beanFactory, new PaymentDeclinedException());

    assertThat(problem).isEqualTo(Problem.builder().status(402).build());
  }

  @Test
  void givenHandlerWithExceptionClasses_whenProcessing_thenRegistersResolverPerClass() {
    DefaultListableBeanFactory beanFactory = process(ExceptionClassesHandlers.class);

    assertThat(resolverFor(beanFactory, OrderNotFoundException.class).getExceptionClass())
        .isEqualTo(OrderNotFoundException.class);
    assertThat(resolverFor(beanFactory, CustomerNotFoundException.class).getExceptionClass())
        .isEqualTo(CustomerNotFoundException.class);
    assertThat(beanFactory.getBeanNamesForType(ProblemResolver.class)).hasSize(2);
  }

  @Test
  void givenHandlerWithExceptionClassesAndNoExceptionParameter_whenResolving_thenResolvesProblem() {
    DefaultListableBeanFactory beanFactory = process(NoExceptionParameterHandlers.class);

    Problem problem = resolve(beanFactory, new PaymentDeclinedException());

    assertThat(problem).isEqualTo(Problem.builder().status(402).build());
  }

  @Test
  void givenInheritedHandler_whenProcessing_thenRegistersResolver() {
    DefaultListableBeanFactory beanFactory = process(InheritingHandlers.class);

    Problem problem = resolve(beanFactory, new PaymentDeclinedException());

    assertThat(problem).isEqualTo(Problem.builder().status(402).build());
  }

  @Test
  void givenHandler_whenProcessing_thenDoesNotInstantiateTargetBean() {
    DefaultListableBeanFactory beanFactory = process(PublicHandlers.class);

    assertThat(beanFactory.getBeanNamesForType(ProblemResolver.class)).hasSize(1);
    assertThat(beanFactory.containsSingleton("handlers")).isFalse();
  }

  @Test
  void givenHandler_whenResolving_thenInstantiatesTargetBean() {
    DefaultListableBeanFactory beanFactory = process(PublicHandlers.class);

    resolve(beanFactory, new PaymentDeclinedException());

    assertThat(beanFactory.containsSingleton("handlers")).isTrue();
  }

  @Test
  void givenJdkProxiedBean_whenResolving_thenInvokesMethodThroughProxy() {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    RootBeanDefinition definition = new RootBeanDefinition(InterfaceHandlersImpl.class);
    definition.setInstanceSupplier(
        () -> {
          ProxyFactory proxyFactory = new ProxyFactory(new InterfaceHandlersImpl());
          proxyFactory.addInterface(InterfaceHandlers.class);
          return proxyFactory.getProxy();
        });
    beanFactory.registerBeanDefinition("handlers", definition);
    new ProblemHandlerMethodProcessor().postProcessBeanFactory(beanFactory);

    Problem problem = resolve(beanFactory, new PaymentDeclinedException());

    assertThat(problem).isEqualTo(Problem.builder().status(402).build());
  }

  @Test
  void givenBeanWithoutHandlers_whenProcessing_thenRegistersNothing() {
    DefaultListableBeanFactory beanFactory = process(PlainBean.class);

    assertThat(beanFactory.getBeanNamesForType(ProblemResolver.class)).isEmpty();
  }

  @Test
  void givenAbstractBeanDefinition_whenProcessing_thenSkipsIt() {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    RootBeanDefinition definition = new RootBeanDefinition(PublicHandlers.class);
    definition.setAbstract(true);
    beanFactory.registerBeanDefinition("handlers", definition);

    new ProblemHandlerMethodProcessor().postProcessBeanFactory(beanFactory);

    assertThat(beanFactory.getBeanNamesForType(ProblemResolver.class)).isEmpty();
  }

  @Test
  void givenBeanDeclaredAsInterface_whenProcessing_thenDoesNotDiscoverHandlers() {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    RootBeanDefinition definition = new RootBeanDefinition();
    definition.setTargetType(Runnable.class);
    definition.setInstanceSupplier(RunnableHandlers::new);
    beanFactory.registerBeanDefinition("handlers", definition);

    new ProblemHandlerMethodProcessor().postProcessBeanFactory(beanFactory);

    assertThat(beanFactory.getBeanNamesForType(ProblemResolver.class)).isEmpty();
  }

  @Test
  void givenHandlerWithOrderAnnotation_whenProcessing_thenResolverHasThatOrder() {
    DefaultListableBeanFactory beanFactory = process(OrderedHandlers.class);

    assertThat(resolverFor(beanFactory, PaymentDeclinedException.class))
        .isInstanceOfSatisfying(
            Ordered.class, ordered -> assertThat(ordered.getOrder()).isEqualTo(5));
  }

  @Test
  void givenHandlerWithoutOrderAnnotation_whenProcessing_thenResolverHasLowestPrecedence() {
    DefaultListableBeanFactory beanFactory = process(PublicHandlers.class);

    assertThat(resolverFor(beanFactory, PaymentDeclinedException.class))
        .isInstanceOfSatisfying(
            Ordered.class,
            ordered -> assertThat(ordered.getOrder()).isEqualTo(Ordered.LOWEST_PRECEDENCE));
  }

  @Test
  void givenPrivateHandler_whenProcessing_thenIgnoresIt() {
    DefaultListableBeanFactory beanFactory = process(PrivateHandlers.class);

    assertThat(beanFactory.getBeanNamesForType(ProblemResolver.class)).hasSize(1);
    assertThat(resolverFor(beanFactory, OutOfStockException.class).getExceptionClass())
        .isEqualTo(OutOfStockException.class);
  }

  @Test
  void givenHandlerWithUnsupportedParameter_whenProcessing_thenFails() {
    assertThatThrownBy(() -> process(UnsupportedParameterHandlers.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("unsupported parameter type java.lang.String");
  }

  @Test
  void givenHandlerWithHttpStatusParameter_whenProcessing_thenFails() {
    assertThatThrownBy(() -> process(HttpStatusParameterHandlers.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("unsupported parameter type org.springframework.http.HttpStatus");
  }

  @Test
  void givenHandlerWithTwoExceptionParameters_whenProcessing_thenFails() {
    assertThatThrownBy(() -> process(TwoExceptionsHandlers.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must declare at most one exception parameter");
  }

  @Test
  void givenHandlerWithoutExceptionParameterOrClasses_whenProcessing_thenFails() {
    assertThatThrownBy(() -> process(NoExceptionHandlers.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must declare an Exception parameter");
  }

  @Test
  void givenHandlerWithErrorParameter_whenProcessing_thenFails() {
    assertThatThrownBy(() -> process(ErrorParameterHandlers.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("unsupported parameter type java.lang.StackOverflowError");
  }

  @Test
  void givenHandlerWithThrowableParameter_whenProcessing_thenFails() {
    assertThatThrownBy(() -> process(ThrowableParameterHandlers.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("unsupported parameter type java.lang.Throwable");
  }

  @Test
  void givenHandlerWithIncompatibleExceptionClass_whenProcessing_thenFails() {
    assertThatThrownBy(() -> process(IncompatibleExceptionClassHandlers.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("is not assignable to parameter");
  }

  @Test
  void givenHandlerWithWrongReturnType_whenProcessing_thenFails() {
    assertThatThrownBy(() -> process(WrongReturnTypeHandlers.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must return io.github.problem4j.core.Problem");
  }

  @Test
  void givenHandlerWithVoidReturnType_whenProcessing_thenFails() {
    assertThatThrownBy(() -> process(VoidReturnTypeHandlers.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must return io.github.problem4j.core.Problem");
  }

  @Test
  void givenHandlerThrowingRuntimeException_whenResolving_thenPropagatesException() {
    DefaultListableBeanFactory beanFactory = process(ThrowingHandlers.class);

    assertThatThrownBy(() -> resolve(beanFactory, new PaymentDeclinedException()))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("broken");
  }

  @Test
  void givenHandlerThrowingCheckedException_whenResolving_thenWrapsException() {
    DefaultListableBeanFactory beanFactory = process(ThrowingHandlers.class);

    assertThatThrownBy(() -> resolve(beanFactory, new OutOfStockException("SKU-4")))
        .isInstanceOf(UndeclaredThrowableException.class)
        .hasCauseInstanceOf(IOException.class);
  }

  @Test
  void givenHandlerReturningNull_whenResolving_thenThrowsIllegalState() {
    DefaultListableBeanFactory beanFactory = process(NullHandlers.class);

    assertThatThrownBy(() -> resolve(beanFactory, new PaymentDeclinedException()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("returned null");
  }

  private static DefaultListableBeanFactory process(Class<?> handlersClass) {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    beanFactory.registerBeanDefinition("handlers", new RootBeanDefinition(handlersClass));
    new ProblemHandlerMethodProcessor().postProcessBeanFactory(beanFactory);
    return beanFactory;
  }

  private static ProblemResolver resolverFor(
      DefaultListableBeanFactory beanFactory, Class<? extends Exception> exceptionClass) {
    return beanFactory.getBeansOfType(ProblemResolver.class).values().stream()
        .filter(resolver -> resolver.getExceptionClass() == exceptionClass)
        .findFirst()
        .orElseThrow();
  }

  private static Problem resolve(DefaultListableBeanFactory beanFactory, Exception ex) {
    return resolverFor(beanFactory, ex.getClass())
        .resolve(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  static class AllParametersHandlers {

    @ProblemHandler
    Problem allParameters(
        OutOfStockException ex,
        ProblemContext context,
        HttpHeaders headers,
        HttpStatusCode status) {
      headers.set("X-Resolved-By", "allParameters");
      return Problem.builder()
          .status(status.value())
          .extension("sku", ex.getSku())
          .extension("traceId", context.get("traceId"))
          .build();
    }
  }

  static class SelectedParametersHandlers {

    @ProblemHandler
    Problem paymentDeclined(PaymentDeclinedException ex) {
      return Problem.builder().status(402).build();
    }

    @ProblemHandler
    Problem outOfStock(OutOfStockException ex, ProblemContext context) {
      return Problem.builder()
          .status(409)
          .extension("sku", ex.getSku())
          .extension("traceId", context.get("traceId"))
          .build();
    }
  }

  static class StatusOnlyHandlers {

    @ProblemHandler(PaymentDeclinedException.class)
    Problem paymentDeclined(HttpStatusCode status) {
      return Problem.builder().status(status.value()).build();
    }
  }

  static class ReorderedParametersHandlers {

    @ProblemHandler
    Problem reordered(
        HttpStatusCode status,
        HttpHeaders headers,
        ProblemContext context,
        OutOfStockException ex) {
      headers.set("X-Resolved-By", "reordered");
      return Problem.builder()
          .status(status.value())
          .extension("sku", ex.getSku())
          .extension("traceId", context.get("traceId"))
          .build();
    }
  }

  static class OrderedHandlers {

    @Order(5)
    @ProblemHandler
    Problem paymentDeclined(PaymentDeclinedException ex) {
      return Problem.builder().status(402).build();
    }
  }

  static class PublicHandlers {

    @ProblemHandler
    public Problem paymentDeclined(PaymentDeclinedException ex) {
      return Problem.builder().status(402).build();
    }
  }

  static class ExceptionClassesHandlers {

    @ProblemHandler({OrderNotFoundException.class, CustomerNotFoundException.class})
    Problem notFound(NotFoundException ex) {
      return Problem.builder().status(404).build();
    }
  }

  static class NoExceptionParameterHandlers {

    @ProblemHandler(PaymentDeclinedException.class)
    Problem paymentDeclined(ProblemContext context) {
      return Problem.builder().status(402).build();
    }
  }

  static class BaseHandlers {

    @ProblemHandler
    Problem paymentDeclined(PaymentDeclinedException ex) {
      return Problem.builder().status(402).build();
    }
  }

  static class InheritingHandlers extends BaseHandlers {}

  interface InterfaceHandlers {

    Problem paymentDeclined(PaymentDeclinedException ex);
  }

  static class InterfaceHandlersImpl implements InterfaceHandlers {

    @ProblemHandler
    @Override
    public Problem paymentDeclined(PaymentDeclinedException ex) {
      return Problem.builder().status(402).build();
    }
  }

  static class PlainBean {

    Problem notAHandler(PaymentDeclinedException ex) {
      return Problem.builder().status(402).build();
    }
  }

  static class RunnableHandlers implements Runnable {

    @ProblemHandler
    Problem paymentDeclined(PaymentDeclinedException ex) {
      return Problem.builder().status(402).build();
    }

    @Override
    public void run() {}
  }

  static class PrivateHandlers {

    @ProblemHandler
    private Problem paymentDeclined(PaymentDeclinedException ex) {
      return Problem.builder().status(402).build();
    }

    @ProblemHandler
    Problem outOfStock(OutOfStockException ex) {
      return Problem.builder().status(409).build();
    }
  }

  static class UnsupportedParameterHandlers {

    @ProblemHandler
    Problem resolve(OutOfStockException ex, String unsupported) {
      return Problem.builder().status(400).build();
    }
  }

  static class HttpStatusParameterHandlers {

    @ProblemHandler
    Problem resolve(OutOfStockException ex, HttpStatus status) {
      return Problem.builder().status(status.value()).build();
    }
  }

  static class TwoExceptionsHandlers {

    @ProblemHandler
    Problem resolve(PaymentDeclinedException ex, OutOfStockException other) {
      return Problem.builder().status(400).build();
    }
  }

  static class NoExceptionHandlers {

    @ProblemHandler
    Problem resolve(ProblemContext context) {
      return Problem.builder().status(400).build();
    }
  }

  static class ErrorParameterHandlers {

    @ProblemHandler
    Problem resolve(StackOverflowError error) {
      return Problem.builder().status(500).build();
    }
  }

  static class ThrowableParameterHandlers {

    @ProblemHandler(PaymentDeclinedException.class)
    Problem resolve(Throwable ex) {
      return Problem.builder().status(500).build();
    }
  }

  static class IncompatibleExceptionClassHandlers {

    @ProblemHandler(PaymentDeclinedException.class)
    Problem resolve(OutOfStockException ex) {
      return Problem.builder().status(400).build();
    }
  }

  static class WrongReturnTypeHandlers {

    @ProblemHandler
    String resolve(OutOfStockException ex) {
      return "not a problem";
    }
  }

  static class VoidReturnTypeHandlers {

    @ProblemHandler
    void resolve(OutOfStockException ex) {}
  }

  static class ThrowingHandlers {

    @ProblemHandler
    Problem paymentDeclined(PaymentDeclinedException ex) {
      throw new UnsupportedOperationException("broken");
    }

    @ProblemHandler
    Problem outOfStock(OutOfStockException ex) throws IOException {
      throw new IOException("checked");
    }
  }

  static class NullHandlers {

    @ProblemHandler
    @Nullable Problem paymentDeclined(PaymentDeclinedException ex) {
      return null;
    }
  }

  static class OutOfStockException extends RuntimeException {

    private final String sku;

    OutOfStockException(String sku) {
      this.sku = sku;
    }

    String getSku() {
      return sku;
    }
  }

  static class PaymentDeclinedException extends RuntimeException {}

  static class NotFoundException extends RuntimeException {}

  static class OrderNotFoundException extends NotFoundException {}

  static class CustomerNotFoundException extends NotFoundException {}
}
