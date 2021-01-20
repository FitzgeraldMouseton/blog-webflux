package com.cheeseind.blogenginewebflux.models.dto.blogdto.tagdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagsResponse {

    private SingleTagDto[] tags;
}
