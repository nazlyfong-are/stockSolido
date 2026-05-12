package com.stockSolido.stockSolido.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
            new StringToLocalTimeConverter()
        ));
    }

    // Convierte String "HH:mm" o "HH:mm:ss" hora local
    static class StringToLocalTimeConverter implements Converter<String, LocalTime> {

        private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm")
            .optionalStart()
            .appendPattern(":ss")
            .optionalEnd()
            .toFormatter();

        @Override
        public LocalTime convert(String source) {
            if (source == null || source.isBlank()) return null;
            return LocalTime.parse(source.trim(), FORMATTER);
        }
    }
}