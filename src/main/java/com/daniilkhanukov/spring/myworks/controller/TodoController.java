package com.daniilkhanukov.spring.myworks.controller;

import com.daniilkhanukov.spring.myworks.dto.TodoDto;
import com.daniilkhanukov.spring.myworks.entity.Todo;
import com.daniilkhanukov.spring.myworks.service.TodoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/todos")
//@RequiredArgsConstructor
//@AllArgsConstructor
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TodoDto>> list(@RequestParam Optional<Boolean> completed) {
        return ResponseEntity.ok(service.getAll(completed));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<TodoDto> create(@Valid @RequestBody TodoDto dto) {
        TodoDto created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoDto> update(@PathVariable Long id, @Valid @RequestBody TodoDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TodoDto> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggle(id));
    }
}
