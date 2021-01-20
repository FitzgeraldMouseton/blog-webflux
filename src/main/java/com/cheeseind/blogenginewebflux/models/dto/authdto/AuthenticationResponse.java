package com.cheeseind.blogenginewebflux.models.dto.authdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse implements Serializable {

    private boolean result;
    private UserLoginResponse user;
}
