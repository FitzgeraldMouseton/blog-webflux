package com.cheeseind.blogenginewebflux.models.dto.blogdto;

import com.cheeseind.blogenginewebflux.models.dto.userdto.UserDto;
import lombok.Data;

@Data
public class ModerationResponse {

    private String id;
    private long timestamp;
    private UserDto user;
    private String title;
    private String announce;
}
