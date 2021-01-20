package com.cheeseind.blogenginewebflux.models.dto.blogdto.votedto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VoteRequest {

    @JsonProperty("post_id")
    private String postId;

    public VoteRequest(String postId) {
        this.postId = postId;
    }
}
