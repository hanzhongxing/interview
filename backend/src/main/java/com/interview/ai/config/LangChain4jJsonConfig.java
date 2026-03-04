package com.interview.ai.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.langchain4j.internal.Json;
import dev.langchain4j.spi.json.JsonCodecFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Custom LangChain4j JsonCodec implementation using Jackson.
 * This bypasses the need for dev.langchain4j:langchain4j-jackson dependency
 * which might be missing in some Maven mirrors.
 */
public class LangChain4jJsonConfig {

    public static class JacksonJsonCodec implements Json.JsonCodec {
        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        @Override
        public String toJson(Object o) {
            try {
                return objectMapper.writeValueAsString(o);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <T> T fromJson(String json, Class<T> type) {
            try {
                return objectMapper.readValue(json, type);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <T> T fromJson(String json, Type type) {
            try {
                return objectMapper.readValue(json, objectMapper.constructType(type));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InputStream toInputStream(Object o, Class<?> type) {
            try {
                return new ByteArrayInputStream(objectMapper.writeValueAsBytes(o));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        // Note: Some versions of LangChain4j might require more methods.
        // If compilation fails, add the missing methods here.
    }

    public static class JacksonJsonCodecFactory implements JsonCodecFactory {
        @Override
        public Json.JsonCodec create() {
            return new JacksonJsonCodec();
        }
    }
}
