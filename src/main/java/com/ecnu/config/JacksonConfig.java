package com.ecnu.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Jackson 序列化配置
 * 将 Long 类型序列化为 String，解决 JavaScript Number 精度丢失问题
 * Snowflake 生成的 ID 超过 JS Number.MAX_SAFE_INTEGER (2^53-1 = 9007199254740991)
 *
 * 序列化: Long → String（前端 JSON 收到字符串，不丢精度）
 * 反序列化: 兼容 Number 和 String → Long（前端传回的字符串/数字都能转为 Long）
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // Long → String 序列化
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);

            // Long 反序列化：兼容 Number 和 String
            JsonDeserializer<Long> longDeserializer = new JsonDeserializer<>() {
                @Override
                public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    // 处理 JSON null
                    if (p.getCurrentToken() == com.fasterxml.jackson.core.JsonToken.VALUE_NULL) return null;
                    String text = p.getText();
                    if (text == null || text.isEmpty() || "null".equals(text)) return null;
                    return Long.valueOf(text);
                }
            };
            builder.deserializerByType(Long.class, longDeserializer);
            builder.deserializerByType(Long.TYPE, longDeserializer);
        };
    }
}
