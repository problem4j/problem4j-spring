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

package io.github.problem4j.spring.webflux.app;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.boot.http.codec.CodecCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.xml.JacksonXmlDecoder;
import org.springframework.http.codec.xml.JacksonXmlEncoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.dataformat.xml.XmlMapper;

@TestConfiguration(proxyBeanMethods = false)
public class XmlCodecTestConfiguration {

  @Bean
  CodecCustomizer problemXmlCodecCustomizer(XmlMapper xmlMapper) {
    return configurer -> {
      CodecConfigurer.DefaultCodecs defaults = configurer.defaultCodecs();
      defaults.jacksonXmlDecoder(new JacksonXmlDecoder(xmlMapper));
      defaults.jacksonXmlEncoder(
          new JacksonXmlEncoder(xmlMapper) {
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
          });
    };
  }
}
