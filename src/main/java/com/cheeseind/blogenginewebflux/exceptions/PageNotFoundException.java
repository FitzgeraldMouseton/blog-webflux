package com.cheeseind.blogenginewebflux.exceptions;

public class PageNotFoundException extends RuntimeException {

    public PageNotFoundException() {
    }

    public PageNotFoundException(String message) {
        super(message);
    }
}
