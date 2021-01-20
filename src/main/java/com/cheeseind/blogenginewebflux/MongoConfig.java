package com.cheeseind.blogenginewebflux;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Configuration
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    @Bean
    @Override
    public MongoCustomConversions customConversions() {
        List<Converter<?,?>> converters = new ArrayList<>();
        converters.add(DateToZonedDateTimeConverter.INSTANCE);
        converters.add(ZonedDateTimeToDateConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }

    enum DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {

        INSTANCE;

        @Override
        public ZonedDateTime convert(Date source) {
            return source.toInstant().atZone(ZoneOffset.UTC);
        }
    }

    enum ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {

        INSTANCE;

        @Override
        public Date convert(ZonedDateTime source) {
            return Date.from(source.toInstant());
        }
    }

    @Override
    protected String getDatabaseName() {
        return "blogmongodb";
    }
}