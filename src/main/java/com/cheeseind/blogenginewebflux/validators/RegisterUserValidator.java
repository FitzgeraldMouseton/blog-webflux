package com.cheeseind.blogenginewebflux.validators;

import com.cheeseind.blogenginewebflux.models.constants.UserConstraints;
import com.cheeseind.blogenginewebflux.models.dto.authdto.RegisterRequest;
import com.cheeseind.blogenginewebflux.services.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserValidator implements Validator {

    private final CaptchaService captchaService;

    private final EmailValidator emailValidator;

    @Override
    public boolean supports(Class aClass) {
        return RegisterRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        RegisterRequest request = (RegisterRequest) o;
        if (request.getName().trim().length() < UserConstraints.MIN_USER_NAME_LENGTH) {
            errors.rejectValue(
                    "name",
                    "field.min.length",
                    new Object[]{3},
                    "Имя должно быть не короче " + UserConstraints.MIN_USER_NAME_LENGTH + " символов"
            );
        }

        if (request.getPassword().trim().length() < UserConstraints.MIN_PASSWORD_LENGTH) {
            errors.rejectValue(
                    "password",
                    "field.min.length",
                    new Object[]{3},
                    "Пароль должен быть не короче " + UserConstraints.MIN_PASSWORD_LENGTH + " символов"
            );
        }

        //TODO как я понимаю, когда эта часть кода вызывается в хэндлере методом validate,
        // то, вследствие асинхронности, эта часть не успевает выполниться, и валидатор не входит
        // в тот блок, в который должен входить при наличии ошибок, т.к. ошибка еще
        // не успевает появиться. Поэтому поставили пока паузу в одну секунду в валидаторе.
        // Возможно, придется просто перенести эту проверку в логику самого метода регистрации.
        // Варианта как-то ловко подписаться что-то не найти.
//        emailValidator.validate(request.getEmail(), errors);

        // Вариант из документации
        try {
            errors.pushNestedPath("address");
            ValidationUtils.invokeValidator(this.emailValidator, request.getEmail(), errors);
        } finally {
            errors.popNestedPath();
        }

        // TODO такая же фигня
        captchaService.findBySecretCode(request.getCaptchaSecret())
                .doOnNext(captchaCode -> {
                    if (captchaCode == null) {
                        ValidationUtils.rejectIfEmpty(errors,
                                "captcha",
                                "field.min.length",
                                "Код устарел"
                        );
                    } else if (!captchaCode.getCode().equals(request.getCaptcha())) {
                        errors.rejectValue(
                                "captcha",
                                "field.min.length",
                                "Код введен неправильно"
                        );
                    }
                }).subscribe();
    }
}
