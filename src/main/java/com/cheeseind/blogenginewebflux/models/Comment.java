package com.cheeseind.blogenginewebflux.models;

import com.cheeseind.blogenginewebflux.models.constants.PostConstraints;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@Document(collection = "comments")
public class Comment {

    public Comment(@Size(min = PostConstraints.MIN_COMMENT_SIZE) String text) {
        this.text = text;
    }

    @Id
    private String id;

    private String postId;

    private String parent;

    private String parentId;

//    @DBRef
    private User user;

    private ZonedDateTime time;

    private String text;
}
