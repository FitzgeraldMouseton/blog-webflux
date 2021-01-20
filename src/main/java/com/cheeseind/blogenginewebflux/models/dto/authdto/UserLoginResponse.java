package com.cheeseind.blogenginewebflux.models.dto.authdto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginResponse implements Serializable {

    private String id;
    private String name;
    private String photo;
    private String email;
    private boolean moderation;
    private long moderationCount;
    private boolean settings;
}
