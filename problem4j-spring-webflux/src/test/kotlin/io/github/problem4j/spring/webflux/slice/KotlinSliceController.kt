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

package io.github.problem4j.spring.webflux.slice

import io.github.problem4j.core.ProblemContext
import java.time.Duration
import kotlinx.coroutines.time.delay
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class KotlinSliceController {

  @GetMapping("/slice/kotlin/problem-context")
  fun problemContext(context: ProblemContext): String = context["traceId"].toString()

  @GetMapping("/slice/kotlin/suspend/problem-context")
  suspend fun suspendProblemContext(context: ProblemContext): String {
    delay(Duration.ofMillis(1))
    return context["traceId"].toString()
  }
}
