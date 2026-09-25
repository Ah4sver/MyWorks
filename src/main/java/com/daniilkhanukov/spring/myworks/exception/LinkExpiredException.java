package com.daniilkhanukov.spring.myworks.exception;

public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException(String link) {
        super("Short link '" + link + "' expired");
    }
}
