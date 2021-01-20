package com.cheeseind.blogenginewebflux.models.dto.blogdto.postdto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PostsInfoResponse<T> {

    private Long count;
    private List<T> posts;
}
