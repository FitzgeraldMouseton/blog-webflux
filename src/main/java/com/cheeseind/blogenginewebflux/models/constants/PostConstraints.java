package com.cheeseind.blogenginewebflux.models.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostConstraints {

    public static final int MIN_TITLE_SIZE = 3;
    public static final int MIN_TEXT_SIZE = 50;
    public static final int MIN_COMMENT_SIZE = 5;
    public static final int ANNOUNCE_LENGTH = 200;
}
