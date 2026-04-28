package com.daniilkhanukov.spring.myworks.api;


import com.daniilkhanukov.spring.myworks.dto.TodoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "todo", description = "TODO API")
public interface TodoApi {

    @Operation(summary = "List todos with pagination and optional completed filter")
    @GetMapping("/api/todos")
    List<TodoDto> list(@RequestParam(required = false) Boolean completed,
                       @RequestParam(defaultValue = "20") int limit,
                       @RequestParam(defaultValue = "0") int offset);

    @Operation(summary = "Get todo")
    @GetMapping("/api/todos/{id}")
    TodoDto get(@PathVariable Long id);

    @Operation(summary = "Create todo")
    @PostMapping("/api/todos")
    @ResponseStatus(code = HttpStatus.CREATED)
    TodoDto create(@Valid @RequestBody TodoDto dto, HttpServletResponse response);

    @Operation(summary = "Update todo")
    @PutMapping("/api/todos/{id}")
    TodoDto update(@PathVariable Long id, @Valid @RequestBody TodoDto dto);

    @Operation(summary = "Delete todo")
    @DeleteMapping("/api/todos/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id);

    @Operation(summary = "Toggle completed")
    @PatchMapping("/api/todos/{id}/toggle")
    TodoDto toggle(@PathVariable Long id);
}
