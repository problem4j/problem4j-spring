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
