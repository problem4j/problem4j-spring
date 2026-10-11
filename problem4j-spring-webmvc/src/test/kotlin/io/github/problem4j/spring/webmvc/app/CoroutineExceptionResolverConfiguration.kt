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

package io.github.problem4j.spring.webmvc.app

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.resolver.AbstractProblemResolver
import io.github.problem4j.spring.web.resolver.ProblemResolver
import io.github.problem4j.spring.webmvc.app.problem.CoroutineException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode

@Configuration(proxyBeanMethods = false)
class CoroutineExceptionResolverConfiguration {

  @Bean fun coroutineExceptionResolver(): ProblemResolver = CoroutineExceptionResolver()

  class CoroutineExceptionResolver : AbstractProblemResolver(CoroutineException::class.java) {

    override fun resolve(
        context: ProblemContext,
        ex: Exception,
        headers: HttpHeaders,
        status: HttpStatusCode,
    ): Problem =
        Problem.builder()
            .type("https://example.org/coroutine")
            .status(409)
            .detail(ex.message)
            .build()
  }
}
