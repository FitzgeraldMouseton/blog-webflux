package com.cheeseind.blogenginewebflux.models.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserConstraints {

    public static final int MIN_USER_NAME_LENGTH = 2;
    public static final int MIN_PASSWORD_LENGTH = 6;
}
