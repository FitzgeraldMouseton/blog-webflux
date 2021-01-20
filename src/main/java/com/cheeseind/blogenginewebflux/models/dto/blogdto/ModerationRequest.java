package com.cheeseind.blogenginewebflux.models.dto.blogdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModerationRequest {

    @JsonProperty("post_id")
    private String postId;
    private String decision;
}
