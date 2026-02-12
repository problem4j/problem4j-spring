/*
 * Copyright (c) 2025-2026 Damian Malczewski
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.BindParam;

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
   * Converts a {@link FieldError} from a {@link BindingResult} into a {@link Violation}.
   *
   * <p>Resolves a field error into a Violation, taking into account {@link BindParam} annotations
   * on the target object's constructor parameters.
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
    Map<String, String> parametersMetadata = findParametersMetadata(bindingResult);
    String field = parametersMetadata.getOrDefault(error.getField(), error.getField());
    if (error.isBindingFailure()) {
      return new Violation(field, IS_NOT_VALID_ERROR);
    } else {
      return new Violation(field, error.getDefaultMessage());
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

  /**
   * Reads metadata mapping for the target object of a BindingResult.
   *
   * @param bindingResult the BindingResult containing the target object
   * @return an unmodifiable map of parameter names to their bound names, or empty map if target is
   *     {@code null}
   */
  protected Map<String, String> findParametersMetadata(BindingResult bindingResult) {
    if (bindingResult.getTarget() != null) {
      Class<?> target = bindingResult.getTarget().getClass();
      return computeConstructorMetadata(target);
    }
    return Map.of();
  }

  /**
   * Computes constructor metadata for the given class.
   *
   * @param target the class to analyze
   * @return an unmodifiable map of constructor parameter names to their bound names
   */
  protected Map<String, String> computeConstructorMetadata(Class<?> target) {
    return findBindingConstructor(target)
        .filter(c -> c.getParameters().length > 0)
        .map(this::getConstructorParameterMetadata)
        .orElseGet(Map::of);
  }

  /**
   * Finds the constructor that most likely was used for binding for the given class.
   *
   * <p>For records, returns the canonical constructor. For non-records, returns the single declared
   * constructor if only one exists.
   *
   * @param target the class to inspect
   * @return an {@code Optional} containing the binding constructor if found
   */
  protected Optional<Constructor<?>> findBindingConstructor(Class<?> target) {
    if (target.isRecord()) {
      Class<?>[] mainArgs =
          Arrays.stream(target.getRecordComponents())
              .map(RecordComponent::getType)
              .toArray(i -> new Class<?>[i]);
      try {
        return Optional.of(target.getDeclaredConstructor(mainArgs));
      } catch (NoSuchMethodException e) {
        return Optional.empty();
      }
    } else {
      // Non records are required to have single constructor anyway, otherwise binding will fail
      // and this code won't be called anyway
      Constructor<?>[] ctors = target.getDeclaredConstructors();
      if (ctors.length == 1) {
        return Optional.of(ctors[0]);
      }
    }
    return Optional.empty();
  }

  /**
   * Extracts parameter metadata from the given constructor.
   *
   * <p>Each constructor parameter is added with its parameter name. {@link BindParam} is taken into
   * account if present.
   *
   * @param constructor the constructor to inspect
   * @return an unmodifiable map of parameter names to their bound names
   */
  protected Map<String, String> getConstructorParameterMetadata(Constructor<?> constructor) {
    Annotation[][] annotations = constructor.getParameterAnnotations();
    Parameter[] parameters = constructor.getParameters();

    Map<String, String> metadata = new HashMap<>();
    for (int i = 0; i < parameters.length; i++) {
      String rawParamName = parameters[i].getName();
      metadata.put(rawParamName, rawParamName);

      for (Annotation annotation : annotations[i]) {
        if (annotation instanceof BindParam bindParam) {
          String bindParamName = bindParam.value();
          metadata.put(rawParamName, bindParamName);
        }
      }
    }
    return Collections.unmodifiableMap(metadata);
  }
}
