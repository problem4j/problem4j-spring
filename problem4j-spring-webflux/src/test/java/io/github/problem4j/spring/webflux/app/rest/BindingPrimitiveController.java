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

package io.github.problem4j.spring.webflux.app.rest;

import io.github.problem4j.spring.webflux.app.model.PrimitiveModel;
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
