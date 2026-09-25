package com.daniilkhanukov.spring.myworks.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String link) {
        super("Short link '" + link + "' not found");
    }
}
