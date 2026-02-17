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

package io.github.problem4j.spring.web.parameter;

import static io.github.problem4j.spring.web.ProblemSupport.IS_NOT_VALID_ERROR;

import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/** Default implementation of {@link BindingResultSupport}. */
public class DefaultBindingResultSupport implements BindingResultSupport {

  /**
   * Builds a {@link Violation}s list from a Spring {@link BindingResult} (e.g. produced when
   * binding a {@code @ModelAttribute} fails or when {@code @Valid} detects field / global errors).
   * Field errors are translated into {@link Violation}s keyed by field name; global errors use
   * {@code null} as the field name.
   *
   * @param result the binding/validation result to convert (must not be {@code null})
   * @return list of violations extracted from the binding result
   */
  @Override
  public List<Violation> fetchViolations(BindingResult result) {
    List<Violation> errors = new ArrayList<>();
    result.getFieldErrors().forEach(f -> errors.add(resolveFieldError(result, f)));
    result.getGlobalErrors().forEach(g -> errors.add(resolveGlobalError(result, g)));
    return errors;
  }

  /**
   * Converts a {@link FieldError} from a {@link BindingResult} into a {@link Violation}. *
   *
   * <p>{@code isBindingFailure() == true} usually means that there was a failure in creation of
   * object from values taken out of request. Most common one is validation error or type mismatch
   * between {@code @ModelAttribute}-annotated argument and one of its values.
   *
   * @param bindingResult the {@link BindingResult} containing the validation errors
   * @param error the {@link FieldError} to convert
   * @return a {@link Violation} representing the field error
   */
  protected Violation resolveFieldError(BindingResult bindingResult, FieldError error) {
    if (error.isBindingFailure()) {
      return new Violation(error.getField(), IS_NOT_VALID_ERROR);
    } else {
      return new Violation(error.getField(), error.getDefaultMessage());
    }
  }

  /**
   * Converts a global {@link ObjectError} from a {@link BindingResult} into a {@link Violation}.
   *
   * <p>A global error is not associated with a specific field, so the {@code field} property of the
   * resulting {@link Violation} is set to {@code null}. The {@code message} is taken from the
   * error's default message.
   *
   * @param bindingResult the {@link BindingResult} containing the validation errors
   * @param error the {@link ObjectError} to convert
   * @return a {@link Violation} representing the global error
   */
  protected Violation resolveGlobalError(BindingResult bindingResult, ObjectError error) {
    return new Violation(null, error.getDefaultMessage());
  }
}
