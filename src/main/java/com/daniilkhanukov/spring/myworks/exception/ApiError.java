package com.daniilkhanukov.spring.myworks.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private String error;
    private Object details;

    public ApiError(String error, Object details) {
        this.error = error;
        this.details = details;
    }

    public ApiError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public Object getDetails() {
        return details;
    }
}
