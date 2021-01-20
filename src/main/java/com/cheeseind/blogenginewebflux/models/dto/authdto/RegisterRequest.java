package com.cheeseind.blogenginewebflux.models.dto.authdto;

import com.cheeseind.blogenginewebflux.models.dto.Validatable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest implements Validatable {

    @JsonProperty("e_mail")
    private String email;

    private String name;

    private String password;

    private String captcha;

    @JsonProperty("captcha_secret")
    private String captchaSecret;
}
