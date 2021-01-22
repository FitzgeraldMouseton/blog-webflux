package com.cheeseind.blogenginewebflux.util.mongodateconverters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

//@Component
@WritingConverter
public class ZonedDateTimeWriteConverter implements Converter<Date, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(Date date) {
        return date.toInstant().atZone(ZoneOffset.UTC);
    }


}
