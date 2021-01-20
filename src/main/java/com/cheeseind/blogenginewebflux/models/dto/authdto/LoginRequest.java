package com.cheeseind.blogenginewebflux.models.dto.authdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email
    @JsonProperty("e_mail")
    private String email;
    private String password;
}
