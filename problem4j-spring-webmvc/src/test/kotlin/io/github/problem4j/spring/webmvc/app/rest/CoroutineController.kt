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

package io.github.problem4j.spring.webmvc.app.rest

import io.github.problem4j.core.ProblemException
import io.github.problem4j.spring.web.buildProblem
import io.github.problem4j.spring.webmvc.app.problem.CoroutineException
import io.github.problem4j.spring.webmvc.app.problem.UnresolvableException
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/coroutine"])
class CoroutineController {

  @GetMapping(path = ["/problem-exception"])
  suspend fun problemException(): String {
    withContext(Dispatchers.Default) { delay(Duration.ofMillis(1)) }
    throw ProblemException(
        buildProblem {
          type("https://example.org/coroutine")
          status(409)
          detail("coroutine conflict")
        }
    )
  }

  @GetMapping(path = ["/resolver"])
  suspend fun resolver(): String =
      withContext(Dispatchers.IO) { throw CoroutineException("coroutine failure") }

  @GetMapping(path = ["/unresolvable"])
  suspend fun unresolvable(): String {
    delay(Duration.ofMillis(1))
    throw UnresolvableException()
  }

  @GetMapping(path = ["/flow"])
  fun flow(): Flow<String> = flow {
    delay(Duration.ofMillis(1))
    throw CoroutineException("flow failure")
  }
}
