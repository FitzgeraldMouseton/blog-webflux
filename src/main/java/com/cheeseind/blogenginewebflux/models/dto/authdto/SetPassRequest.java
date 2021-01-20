package com.cheeseind.blogenginewebflux.models.dto.authdto;

import com.cheeseind.blogenginewebflux.models.dto.Validatable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetPassRequest implements Validatable {

    private String password;

    private String code;

    private String captcha;

    @JsonProperty("captcha_secret")
    private String captchaSecret;
}
