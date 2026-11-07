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

import io.github.problem4j.spring.webmvc.app.model.KotlinModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/binding-kotlin")
public class BindingKotlinController {

  @PostMapping(path = "/int", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String intValue(@RequestBody KotlinModel.KotlinIntRequest request) {
    return "OK";
  }

  @PostMapping(path = "/long", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String longValue(@RequestBody KotlinModel.KotlinLongRequest request) {
    return "OK";
  }

  @PostMapping(path = "/short", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String shortValue(@RequestBody KotlinModel.KotlinShortRequest request) {
    return "OK";
  }

  @PostMapping(path = "/byte", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String byteValue(@RequestBody KotlinModel.KotlinByteRequest request) {
    return "OK";
  }

  @PostMapping(path = "/float", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String floatValue(@RequestBody KotlinModel.KotlinFloatRequest request) {
    return "OK";
  }

  @PostMapping(path = "/double", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String doubleValue(@RequestBody KotlinModel.KotlinDoubleRequest request) {
    return "OK";
  }

  @PostMapping(path = "/boolean", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String booleanValue(@RequestBody KotlinModel.KotlinBooleanRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/int", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedInt(@RequestBody KotlinModel.KotlinNestedIntRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/long", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedLong(@RequestBody KotlinModel.KotlinNestedLongRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/short", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedShort(@RequestBody KotlinModel.KotlinNestedShortRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/byte", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedByte(@RequestBody KotlinModel.KotlinNestedByteRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/float", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedFloat(@RequestBody KotlinModel.KotlinNestedFloatRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/double", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedDouble(@RequestBody KotlinModel.KotlinNestedDoubleRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nested/boolean", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nestedBoolean(@RequestBody KotlinModel.KotlinNestedBooleanRequest request) {
    return "OK";
  }

  @PostMapping(path = "/complex", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String complex(@RequestBody KotlinModel.KotlinComplexRequest request) {
    return "OK";
  }

  @PostMapping(path = "/nullable", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String nullable(@RequestBody KotlinModel.KotlinNullableRequest request) {
    return "OK";
  }

  @PostMapping(path = "/default", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String defaultValue(@RequestBody KotlinModel.KotlinDefaultRequest request) {
    return "OK";
  }

  @PostMapping(path = "/list/non-null-elements", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String listNonNull(@RequestBody KotlinModel.KotlinListNonNullRequest request) {
    return "OK";
  }

  @PostMapping(path = "/list/nullable-elements", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String listNullableElements(
      @RequestBody KotlinModel.KotlinListNullableElementsRequest request) {
    return "OK";
  }

  @PostMapping(path = "/map/non-null-values", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String mapNonNull(@RequestBody KotlinModel.KotlinMapValueNonNullRequest request) {
    return "OK";
  }

  @PostMapping(path = "/map/nullable-values", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String mapNullable(@RequestBody KotlinModel.KotlinMapValueNullableRequest request) {
    return "OK";
  }
}
