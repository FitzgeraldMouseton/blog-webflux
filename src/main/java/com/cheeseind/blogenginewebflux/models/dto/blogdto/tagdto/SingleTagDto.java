package com.cheeseind.blogenginewebflux.models.dto.blogdto.tagdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleTagDto {

    private String name;
    private float weight;
}
