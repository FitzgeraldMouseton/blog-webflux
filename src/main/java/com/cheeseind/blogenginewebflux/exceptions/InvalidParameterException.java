package com.cheeseind.blogenginewebflux.exceptions;

import org.springframework.validation.Errors;

public class InvalidParameterException extends RuntimeException {

    private Errors errors;

    public InvalidParameterException() {
    }

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Errors errors) {
        super(message);
        this.errors = errors;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }
}
