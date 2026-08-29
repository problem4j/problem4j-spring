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

package io.github.problem4j.spring.webmvc.app.model

interface KotlinModel {

  data class KotlinIntRequest(val value: Int)

  data class KotlinLongRequest(val value: Long)

  data class KotlinShortRequest(val value: Short)

  data class KotlinByteRequest(val value: Byte)

  data class KotlinFloatRequest(val value: Float)

  data class KotlinDoubleRequest(val value: Double)

  data class KotlinBooleanRequest(val value: Boolean)

  data class KotlinNestedIntRequest(val nested: KotlinIntRequest)

  data class KotlinNestedLongRequest(val nested: KotlinLongRequest)

  data class KotlinNestedShortRequest(val nested: KotlinShortRequest)

  data class KotlinNestedByteRequest(val nested: KotlinByteRequest)

  data class KotlinNestedFloatRequest(val nested: KotlinFloatRequest)

  data class KotlinNestedDoubleRequest(val nested: KotlinDoubleRequest)

  data class KotlinNestedBooleanRequest(val nested: KotlinBooleanRequest)

  data class KotlinTreeRequest(val leaf: KotlinNestedShortRequest)

  data class KotlinComplexRequest(
      val flag: Boolean,
      val timestamp: Long,
      val amount: Double,
      val shortNested: KotlinShortRequest,
      val tree: KotlinTreeRequest,
  )

  data class KotlinNullableRequest(val value: Int?)

  data class KotlinDefaultRequest(val x: Int = 5)

  data class KotlinListNonNullRequest(val values: List<Int>)

  data class KotlinListNullableElementsRequest(val values: List<Int?>)

  data class KotlinMapValueNonNullRequest(val map: Map<String, Int>)

  data class KotlinMapValueNullableRequest(val map: Map<String, Int?>)
}
