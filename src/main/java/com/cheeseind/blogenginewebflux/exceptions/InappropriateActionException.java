package com.cheeseind.blogenginewebflux.exceptions;

public class InappropriateActionException extends RuntimeException {

    public InappropriateActionException() {
    }

    public InappropriateActionException(String message) {
        super(message);
    }
}
