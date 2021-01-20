package com.cheeseind.blogenginewebflux.validators;

import com.cheeseind.blogenginewebflux.models.CaptchaCode;
import com.cheeseind.blogenginewebflux.models.dto.authdto.SetPassRequest;
import com.cheeseind.blogenginewebflux.services.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetNewPasswordValidator implements Validator {

    private final CaptchaService captchaService;

    @Value("${restore_code.expiration_time}")
    private Integer restoreCodeExpirationTime;
    @Value("${restore_code.code_length}")
    private int restoreCodeLength;

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    @Override
    public void validate(Object o, Errors errors) {
        SetPassRequest request = (SetPassRequest) o;
        String code = request.getCode();
        String encodedTime = Objects.requireNonNull(code).substring(restoreCodeLength);
        String decodedTime = new String(Base64.getDecoder().decode(encodedTime));
        long time = Long.parseLong(decodedTime);
        Instant currentTime = Instant.now();
        Instant codeTime = Instant.ofEpochMilli(time);
        if (code.isEmpty() || currentTime.isAfter(codeTime.plusSeconds(restoreCodeExpirationTime))) {
            errors.rejectValue(
                    "code",
                    "field.min.length",
                    new Object[]{3},
                    "Ссылка для восстановления пароля устарела.<a href=\"/auth/restore\">Запросить ссылку снова</a>"
            );
        }

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
