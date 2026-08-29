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

package io.github.problem4j.spring.web;

import io.github.problem4j.spring.web.config.ProblemBeanPostProcessor;

/**
 * Common super-interface of every {@code *Aware} callback that {@link ProblemBeanPostProcessor}
 * honours ({@link ProblemFormatAware}, {@link TypeNameMapperAware}, {@link
 * io.github.problem4j.spring.web.parameter.BindingResultSupportAware BindingResultSupportAware},
 * {@link io.github.problem4j.spring.web.parameter.MethodValidationResultSupportAware
 * MethodValidationResultSupportAware}, {@link
 * io.github.problem4j.spring.web.parameter.MethodParameterSupportAware
 * MethodParameterSupportAware}).
 *
 * <p>It carries no methods; it lets {@link ProblemBeanPostProcessor} skip any bean that opts into
 * none of the callbacks with a single {@code instanceof} check.
 *
 * @since 3.1.0
 */
public interface ProblemSupportAware {}
