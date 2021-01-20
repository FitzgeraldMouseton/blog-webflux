package com.cheeseind.blogenginewebflux.validators;

import com.cheeseind.blogenginewebflux.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class EmailValidator implements Validator {

    private final UserService userService;

    @Override
    public boolean supports(Class<?> aClass) {
        return String.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        String email = (String) o;
        userService.findByEmail(email)
                .doOnNext(user -> {
                    if (user != null) {
                        errors.rejectValue(
                                "email",
                                "field.min.length",
                                new Object[]{3},
                                "Пользователь с таким адресом уже зарегистрирован"
                        );
                    }
                }).subscribe();
    }
}
