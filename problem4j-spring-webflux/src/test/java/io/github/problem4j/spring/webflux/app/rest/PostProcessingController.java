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

package io.github.problem4j.spring.webflux.app.rest;

import io.github.problem4j.core.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/post-processing")
public class PostProcessingController {

  @GetMapping(path = "/problem")
  public Problem problem() {
    return Problem.builder().title("Conflict").status(409).detail("returned problem").build();
  }

  @GetMapping(path = "/problem-detail")
  public ProblemDetail problemDetail() {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "returned problem detail");
  }

  @GetMapping(path = "/problem-mono")
  public Mono<Problem> problemMono() {
    return Mono.fromSupplier(this::problem);
  }

  @GetMapping(path = "/problem-detail-mono")
  public Mono<ProblemDetail> problemDetailMono() {
    return Mono.fromSupplier(this::problemDetail);
  }

  @GetMapping(path = "/problem-entity")
  public ResponseEntity<Problem> problemEntity() {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem());
  }

  @GetMapping(path = "/problem-detail-entity")
  public ResponseEntity<ProblemDetail> problemDetailEntity() {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail());
  }

  @GetMapping(path = "/problem-object-entity")
  public ResponseEntity<Object> problemObjectEntity() {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem());
  }

  @GetMapping(path = "/error-response")
  public ErrorResponse errorResponse() {
    return ErrorResponse.create(
        new IllegalStateException(), HttpStatus.CONFLICT, "returned error response");
  }

  @GetMapping(path = "/problem-entity-mono")
  public Mono<ResponseEntity<Problem>> problemEntityMono() {
    return Mono.fromSupplier(this::problemEntity);
  }

  @GetMapping(path = "/problem-mono-entity")
  public ResponseEntity<Mono<Problem>> problemMonoEntity() {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemMono());
  }

  @GetMapping(path = "/problem-detail-mono-entity")
  public ResponseEntity<Mono<ProblemDetail>> problemDetailMonoEntity() {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetailMono());
  }

  @GetMapping(path = "/local-handler")
  public String localHandler() {
    throw new LocalHandledException();
  }

  @GetMapping(path = "/local-entity-handler")
  public String localEntityHandler() {
    throw new LocalEntityHandledException();
  }

  @GetMapping(path = "/advice-handler")
  public String adviceHandler() {
    throw new AdviceHandledException();
  }

  @ExceptionHandler(LocalHandledException.class)
  public Problem handleLocal(LocalHandledException ex) {
    return Problem.builder().title("Bad Request").status(400).detail("local handler").build();
  }

  @ExceptionHandler(LocalEntityHandledException.class)
  public ResponseEntity<Problem> handleLocalEntity(LocalEntityHandledException ex) {
    return ResponseEntity.badRequest()
        .body(Problem.builder().title("Bad Request").status(400).detail("local handler").build());
  }

  public static class LocalHandledException extends RuntimeException {}

  public static class LocalEntityHandledException extends RuntimeException {}

  public static class AdviceHandledException extends RuntimeException {}
}
