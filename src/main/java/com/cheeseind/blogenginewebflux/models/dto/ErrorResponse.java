package com.cheeseind.blogenginewebflux.models.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ErrorResponse {

    private boolean result = false;
    private Map<String, String> errors;

    public ErrorResponse(Map<String, String> errors) {
        this.errors = errors;
    }
}
