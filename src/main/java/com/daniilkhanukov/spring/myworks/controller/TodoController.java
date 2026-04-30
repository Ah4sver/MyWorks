package com.daniilkhanukov.spring.myworks.controller;

import com.daniilkhanukov.spring.myworks.api.TodoApi;
import com.daniilkhanukov.spring.myworks.dto.TodoDto;
import com.daniilkhanukov.spring.myworks.entity.Todo;
import com.daniilkhanukov.spring.myworks.service.TodoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class TodoController implements TodoApi {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @Override
    public List<TodoDto> list(Boolean completed, int limit, int offset) {
        return service.getAll(completed, limit, offset);
    }

    @Override
    public TodoDto get(Long id) {
        return service.getById(id);
    }

    @Override
    public TodoDto create(TodoDto dto, HttpServletResponse response) {
        TodoDto created = service.create(dto);
        // установить Location header без ResponseEntity
        response.setHeader("Location", "/api/todos/" + created.getId());
        return created;
    }

    @Override
    public TodoDto update(Long id, TodoDto dto) {
        return service.update(id, dto);
    }

    @Override
    public void delete(Long id) {
        service.delete(id);
    }


    @Override
    public TodoDto toggle(Long id) {
        return service.toggle(id);
    }
}
