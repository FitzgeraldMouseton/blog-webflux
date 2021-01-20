package com.cheeseind.blogenginewebflux.models.dto.userdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileRequest {

    private int removePhoto;

    @NotNull(message = "Имя не должно быть пустым")
    @Size(min = 2, message = "Имя не может быть короче 2 символов")
    private String name;

    @NotNull(message = "Поле \"email\" не должно быть пустым")
    private String email;

    //@Size(min = UserConstraints.MIN_PASSWORD_LENGTH, message = "Пароль не может быть короче 6 символов")
    private String password;
}
