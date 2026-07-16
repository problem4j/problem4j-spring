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

package io.github.problem4j.spring.webflux;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.xml.JacksonXmlEncoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.dataformat.xml.XmlMapper;

/**
 * A {@link JacksonXmlEncoder} that supports encoding single-value publishers.
 *
 * <p>Spring's {@link JacksonXmlEncoder} rejects {@link #encode(Publisher, DataBufferFactory,
 * ResolvableType, MimeType, Map)} entirely, which makes it unusable for rendering XML response
 * bodies of annotated controllers and error handlers, as those always pass single-value publishers
 * through that method. This subclass encodes the first (and only) element via {@link
 * #encodeValue(Object, DataBufferFactory, ResolvableType, MimeType, Map)} instead. Stream encoding
 * remains unsupported.
 *
 * @since 3.0.1
 */
public class ProblemJacksonXmlEncoder extends JacksonXmlEncoder {

  /**
   * Creates a new {@code ProblemJacksonXmlEncoder} with the given mapper.
   *
   * @param xmlMapper the XML mapper to use for encoding
   * @since 3.0.1
   */
  public ProblemJacksonXmlEncoder(XmlMapper xmlMapper) {
    super(xmlMapper);
  }

  /**
   * Encodes the single value emitted by the given publisher.
   *
   * @param inputStream the publisher emitting the value to encode; only the first element is
   *     encoded
   * @param bufferFactory the factory for creating data buffers
   * @param elementType the type of the value to encode
   * @param mimeType the target mime type
   * @param hints additional hints for encoding
   * @return a flux emitting the encoded value
   * @since 3.0.1
   */
  @Override
  public Flux<DataBuffer> encode(
      Publisher<?> inputStream,
      DataBufferFactory bufferFactory,
      ResolvableType elementType,
      @Nullable MimeType mimeType,
      @Nullable Map<String, Object> hints) {
    return Mono.from(inputStream)
        .map(value -> encodeValue(value, bufferFactory, elementType, mimeType, hints))
        .flux();
  }
}
