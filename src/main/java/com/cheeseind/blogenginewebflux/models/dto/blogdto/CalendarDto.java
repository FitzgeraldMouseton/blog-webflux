package com.cheeseind.blogenginewebflux.models.dto.blogdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarDto {

    private Integer[] years;
    @JsonProperty("posts")
    private Map<String, Long> postsPerDate;
}
