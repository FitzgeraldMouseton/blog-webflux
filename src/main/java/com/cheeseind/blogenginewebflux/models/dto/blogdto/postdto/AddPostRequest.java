package com.cheeseind.blogenginewebflux.models.dto.blogdto.postdto;

import com.cheeseind.blogenginewebflux.models.dto.Validatable;
import com.cheeseind.blogenginewebflux.models.constants.PostConstraints;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPostRequest implements Validatable {

    private long timestamp;

    @JsonProperty("active")
    private boolean isActive;

    @Size(min = PostConstraints.MIN_TITLE_SIZE, message = "Заголовок не установлен")
    private String title;

    @Size(min = PostConstraints.MIN_TEXT_SIZE, message = "Текст публикации слишком короткий")
    private String text;

    @JsonProperty("tags")
    private Set<String> tagNames;
}
