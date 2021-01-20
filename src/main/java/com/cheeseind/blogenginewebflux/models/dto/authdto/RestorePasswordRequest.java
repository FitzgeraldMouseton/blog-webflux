package com.cheeseind.blogenginewebflux.models.dto.authdto;

import com.cheeseind.blogenginewebflux.models.dto.Validatable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestorePasswordRequest implements Validatable {

    @Email
    private String email;
}
