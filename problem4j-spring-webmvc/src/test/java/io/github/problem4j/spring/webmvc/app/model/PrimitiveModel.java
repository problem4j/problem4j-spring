/*
 * Copyright 2025-2026 The Problem4J Authors
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

package io.github.problem4j.spring.webmvc.app.model;

public interface PrimitiveModel {

  record IntRequest(int value) {}

  record LongRequest(long value) {}

  record ShortRequest(short value) {}

  record ByteRequest(byte value) {}

  record FloatRequest(float value) {}

  record DoubleRequest(double value) {}

  record BooleanRequest(boolean value) {}

  record NestedIntRequest(IntRequest nested) {}

  record NestedLongRequest(LongRequest nested) {}

  record NestedShortRequest(ShortRequest nested) {}

  record NestedByteRequest(ByteRequest nested) {}

  record NestedFloatRequest(FloatRequest nested) {}

  record NestedDoubleRequest(DoubleRequest nested) {}

  record NestedBooleanRequest(BooleanRequest nested) {}

  record ComplexRequest(boolean flag, long timestamp, double amount, ShortRequest shortNested) {}
}
