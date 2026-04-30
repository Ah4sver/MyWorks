package com.daniilkhanukov.spring.myworks.mapper;

import com.daniilkhanukov.spring.myworks.dto.TodoDto;
import com.daniilkhanukov.spring.myworks.entity.Todo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TodoMapper {
    TodoDto toDto(Todo todo);
    Todo toEntity(TodoDto dto);
}
