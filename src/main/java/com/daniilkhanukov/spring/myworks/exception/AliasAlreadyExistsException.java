package com.daniilkhanukov.spring.myworks.exception;

public class AliasAlreadyExistsException extends RuntimeException {
    public AliasAlreadyExistsException(String alias) {
        super("Alias '" + alias + "' already exists");
    }
}
