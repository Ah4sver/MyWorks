package com.daniilkhanukov.spring.myworks.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotFoundExceptionTest {

    @Test
    void constructorShouldStoreMessage() {
        NotFoundException exception = new NotFoundException("Todo not found with id: 10");

        assertThat(exception)
                .hasMessage("Todo not found with id: 10");
    }
}
