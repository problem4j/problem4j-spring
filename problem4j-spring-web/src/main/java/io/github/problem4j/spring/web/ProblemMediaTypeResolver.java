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

package io.github.problem4j.spring.web;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;

/**
 * Resolves the {@code Content-Type} to use for a {@code Problem} response based on the {@code
 * Accept} header sent by the client.
 *
 * <p>Resolution rules, evaluated in the client's preference order:
 *
 * <ul>
 *   <li>{@code application/json} (or any {@code +json} problem type) resolves to {@code
 *       application/problem+json}.
 *   <li>{@code application/xml} (or any {@code +xml} problem/text-xml type) resolves to {@code
 *       application/problem+xml}.
 *   <li>Anything else, including wildcards such as {@code * / *}, or no match at all, falls back to
 *       {@code application/problem+json}.
 * </ul>
 *
 * @since 3.0.1
 */
public final class ProblemMediaTypeResolver {

  private static final List<MediaType> XML_MEDIA_TYPES =
      List.of(MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_PROBLEM_XML);

  private static final List<MediaType> JSON_MEDIA_TYPES =
      List.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_PROBLEM_JSON);

  /**
   * Resolves the {@code Problem} content type from the client's accepted media types.
   *
   * @param acceptedMediaTypes the media types accepted by the client, as parsed from the {@code
   *     Accept} header; order is assumed to reflect client preference
   * @return {@link MediaType#APPLICATION_PROBLEM_XML} if the client prefers XML, {@link
   *     MediaType#APPLICATION_PROBLEM_JSON} otherwise
   * @since 3.0.1
   */
  public static MediaType resolve(List<MediaType> acceptedMediaTypes) {
    List<MediaType> sorted = new ArrayList<>(acceptedMediaTypes);
    MimeTypeUtils.sortBySpecificity(sorted);

    for (MediaType acceptedMediaType : sorted) {
      if (acceptedMediaType.isWildcardType() || acceptedMediaType.isWildcardSubtype()) {
        continue;
      }
      if (matchesAny(acceptedMediaType, XML_MEDIA_TYPES)) {
        return MediaType.APPLICATION_PROBLEM_XML;
      }
      if (matchesAny(acceptedMediaType, JSON_MEDIA_TYPES)) {
        return MediaType.APPLICATION_PROBLEM_JSON;
      }
    }
    return MediaType.APPLICATION_PROBLEM_JSON;
  }

  private static boolean matchesAny(MediaType candidate, List<MediaType> targets) {
    for (MediaType target : targets) {
      if (candidate.isCompatibleWith(target)) {
        return true;
      }
    }
    return false;
  }

  private ProblemMediaTypeResolver() {}
}
