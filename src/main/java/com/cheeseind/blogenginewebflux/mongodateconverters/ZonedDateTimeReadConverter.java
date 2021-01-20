package com.cheeseind.blogenginewebflux.mongodateconverters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

//@Component
@ReadingConverter
public class ZonedDateTimeReadConverter implements Converter<Date, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(Date date) {
        return date.toInstant().atZone(ZoneOffset.UTC);
    }


}