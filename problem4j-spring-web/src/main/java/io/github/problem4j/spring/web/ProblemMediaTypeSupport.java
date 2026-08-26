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
 *   <li>Any subtype named {@code xml} or ending in {@code +xml} (e.g. {@code application/xml},
 *       {@code text/xml}, {@code application/soap+xml}) resolves to {@code
 *       application/problem+xml}.
 *   <li>Any subtype named {@code json} or ending in {@code +json} (e.g. {@code application/json},
 *       {@code application/vnd.api+json}) resolves to {@code application/problem+json}.
 *   <li>Anything else, including wildcards such as {@code * / *}, or no match at all, falls back to
 *       {@code application/problem+json}.
 * </ul>
 *
 * @since 3.0.1
 */
public final class ProblemMediaTypeSupport {

  /**
   * Resolves the {@code Problem} content type from the client's accepted media types.
   *
   * @param acceptedMediaType the media type accepted by the client, as parsed from the {@code
   *     Accept} header; order is assumed to reflect client preference
   * @return {@link MediaType#APPLICATION_PROBLEM_XML} if the client prefers XML, {@link
   *     MediaType#APPLICATION_PROBLEM_JSON} otherwise
   * @since 3.0.1
   */
  public static MediaType resolveAccept(MediaType acceptedMediaType) {
    return resolveAccept(List.of(acceptedMediaType));
  }

  /**
   * Resolves the {@code Problem} content type from the client's accepted media types.
   *
   * @param acceptedMediaTypes the media types accepted by the client, as parsed from the {@code
   *     Accept} header; order is assumed to reflect client preference
   * @return {@link MediaType#APPLICATION_PROBLEM_XML} if the client prefers XML, {@link
   *     MediaType#APPLICATION_PROBLEM_JSON} otherwise
   * @since 3.0.1
   */
  public static MediaType resolveAccept(MediaType... acceptedMediaTypes) {
    return resolveAccept(List.of(acceptedMediaTypes));
  }

  /**
   * Resolves the {@code Problem} content type from the client's accepted media types.
   *
   * @param acceptedMediaTypes the media types accepted by the client, as parsed from the {@code
   *     Accept} header; order is assumed to reflect client preference
   * @return {@link MediaType#APPLICATION_PROBLEM_XML} if the client prefers XML, {@link
   *     MediaType#APPLICATION_PROBLEM_JSON} otherwise
   * @since 3.0.1
   */
  public static MediaType resolveAccept(List<MediaType> acceptedMediaTypes) {
    List<MediaType> sorted = new ArrayList<>(acceptedMediaTypes);
    MimeTypeUtils.sortBySpecificity(sorted);

    for (MediaType acceptedMediaType : sorted) {
      if (isWildcard(acceptedMediaType)) {
        continue;
      }
      if (isXml(acceptedMediaType)) {
        return MediaType.APPLICATION_PROBLEM_XML;
      }
      if (isJson(acceptedMediaType)) {
        return MediaType.APPLICATION_PROBLEM_JSON;
      }
    }
    return MediaType.APPLICATION_PROBLEM_JSON;
  }

  private static boolean isWildcard(MediaType acceptedMediaType) {
    return acceptedMediaType.isWildcardType() || acceptedMediaType.isWildcardSubtype();
  }

  private static boolean isXml(MediaType mediaType) {
    String subtype = mediaType.getSubtype();
    return subtype.equals("xml") || subtype.endsWith("+xml");
  }

  private static boolean isJson(MediaType mediaType) {
    String subtype = mediaType.getSubtype();
    return subtype.equals("json") || subtype.endsWith("+json");
  }

  private ProblemMediaTypeSupport() {}
}
