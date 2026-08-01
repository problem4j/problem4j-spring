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

package io.github.problem4j.spring.webmvc.app.rest;

import io.github.problem4j.spring.webmvc.app.model.PrimitiveModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/binding-primitive")
public class BindingPrimitiveController {

  @PostMapping(path = "/int", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String intValue(@RequestBody PrimitiveModel.IntRequest request) {
    return "OK";
  }

  @PostMapping(path = "/long", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String longValue(@RequestBody PrimitiveModel.LongRequest request) {
    return "OK";
  }

  @PostMapping(path = "/short", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String shortValue(@RequestBody PrimitiveModel.ShortRequest request) {
    return "OK";
  }

  @PostMapping(path = "/byte", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String byteValue(@RequestBody PrimitiveModel.ByteRequest request) {
    return "OK";
  }

  @PostMapping(path = "/float", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String floatValue(@RequestBody PrimitiveModel.FloatRequest request) {
    return "OK";
  }

  @PostMapping(path = "/double", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String doubleValue(@RequestBody PrimitiveModel.DoubleRequest request) {
    return "OK";
  }

  @PostMapping(path = "/boolean", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String booleanValue(@RequestBody PrimitiveModel.BooleanRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/int", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedInt(@RequestBody PrimitiveModel.NestedIntRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/long", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedLong(@RequestBody PrimitiveModel.NestedLongRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/short", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedShort(@RequestBody PrimitiveModel.NestedShortRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/byte", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedByte(@RequestBody PrimitiveModel.NestedByteRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/float", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedFloat(@RequestBody PrimitiveModel.NestedFloatRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/double", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedDouble(@RequestBody PrimitiveModel.NestedDoubleRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/boolean", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedBoolean(@RequestBody PrimitiveModel.NestedBooleanRequest request) {
    return "OK";
  }

  @PostMapping(path = "/complex", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String complex(@RequestBody PrimitiveModel.ComplexRequest request) {
    return "OK";
  }
}
