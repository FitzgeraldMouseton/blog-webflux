package com.cheeseind.blogenginewebflux.util.validators;

import com.cheeseind.blogenginewebflux.models.dto.blogdto.postdto.AddPostRequest;
import com.cheeseind.blogenginewebflux.models.constants.PostConstraints;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Slf4j
@Component
public class AddPostRequestValidator implements Validator {

    @Override
    public boolean supports(Class aClass) {
        return AddPostRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AddPostRequest request = (AddPostRequest) o;
        if (request.getTitle().trim().length() < PostConstraints.MIN_TITLE_SIZE) {
            errors.rejectValue(
                    "title",
                    "field.min.length",
                    new Object[]{3},
                    "Заголовок должен быть длиной не менее " + PostConstraints.MIN_TITLE_SIZE + " символов"
            );
        }
        if (request.getText().trim().length() < PostConstraints.MIN_TEXT_SIZE) {
            errors.rejectValue(
                    "text",
                    "field.min.length",
                    new Object[]{3},
                    "Текст должен быть длиной не менее " + PostConstraints.MIN_TEXT_SIZE + " символов"
            );
        }
    }

/*
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "field.required");
*/
}
