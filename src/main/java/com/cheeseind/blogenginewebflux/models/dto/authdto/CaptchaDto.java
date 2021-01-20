package com.cheeseind.blogenginewebflux.models.dto.authdto;

import lombok.Data;

@Data
public class CaptchaDto {

    private String secret;
    private String image;
}
