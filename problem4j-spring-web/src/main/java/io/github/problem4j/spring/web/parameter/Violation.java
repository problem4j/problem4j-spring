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

package io.github.problem4j.spring.web.parameter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Represents a validation violation with a specific field and its corresponding error message.
 *
 * @since 1.2.0
 */
@JsonInclude(NON_NULL)
public final class Violation implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * The field name associated with the violation.
   *
   * @since 1.2.0
   */
  private final @Nullable String field;

  /**
   * The error message for the violation.
   *
   * @since 1.2.0
   */
  private final @Nullable String error;

  /**
   * Creates a new {@link Violation}.
   *
   * @param field the name of the field that caused the violation
   * @param error the description of the error
   * @since 1.2.0
   */
  @JsonCreator
  public Violation(
      @JsonProperty("field") @Nullable String field,
      @JsonProperty("error") @Nullable String error) {
    this.field = field;
    this.error = error;
  }

  /**
   * Returns the field name associated with this violation.
   *
   * @return the field name
   * @since 1.2.0
   */
  @JsonProperty("field")
  public @Nullable String getField() {
    return field;
  }

  /**
   * Returns the error message associated with this violation.
   *
   * @return the error message
   * @since 1.2.0
   */
  @JsonProperty("error")
  public @Nullable String getError() {
    return error;
  }

  /**
   * Indicates whether some other object is "equal to" this one. Two violations are considered equal
   * if they have the same field and error message.
   *
   * @param obj the reference object with which to compare.
   * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
   * @since 1.2.0
   */
  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Violation that)) {
      return false;
    }
    return Objects.equals(field, that.field) && Objects.equals(error, that.error);
  }

  /**
   * Returns a hash code value for this violation, based on the field and error message.
   *
   * @return a hash code value for this violation
   * @since 1.2.0
   */
  @Override
  public int hashCode() {
    return Objects.hash(field, error);
  }

  /**
   * Returns a string representation of this violation, including the field and error message.
   *
   * @return a string representation of this violation
   * @since 1.2.0
   */
  @Override
  public String toString() {
    return "Violation[field=" + field + ", error=" + error + "]";
  }
}
