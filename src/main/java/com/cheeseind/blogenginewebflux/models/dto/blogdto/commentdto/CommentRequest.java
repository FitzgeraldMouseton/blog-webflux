package com.cheeseind.blogenginewebflux.models.dto.blogdto.commentdto;

import com.cheeseind.blogenginewebflux.models.constants.PostConstraints;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {

    @JsonProperty("parent_id")
    private String parentId;

    @JsonProperty("post_id")
    private String postId;

    @Size(min = PostConstraints.MIN_COMMENT_SIZE, message = "Текст комментария не задан или слишком короткий")
    private String text;
}
