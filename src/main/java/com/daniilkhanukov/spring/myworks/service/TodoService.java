package com.daniilkhanukov.spring.myworks.service;

import com.daniilkhanukov.spring.myworks.dto.TodoDto;
import com.daniilkhanukov.spring.myworks.entity.Todo;
import com.daniilkhanukov.spring.myworks.exception.NotFoundException;
import com.daniilkhanukov.spring.myworks.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository repository;

    public List<TodoDto> getAll(Optional<Boolean> completed) {
        List<Todo> list = completed.map(repository::findByCompleted).orElseGet(repository::findAll);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public TodoDto getById(Long id) {
        Todo dto = repository.findById(id).orElseThrow(() -> new NotFoundException("Todo not found with id: " + id));
        return toDto(dto);
    }

    public TodoDto create(TodoDto dto) {
        Todo todo = new Todo();
        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());
        todo.setCompleted(dto.getCompleted() != null ? dto.getCompleted() : false);
        Todo saved = repository.save(todo);
        return toDto(saved);
    }

    public TodoDto update(Long id, TodoDto dto) {
        Todo todo = repository.findById(id).orElseThrow(() -> new NotFoundException("Todo not found with id: " + id));
        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());
        if (dto.getCompleted() != null) {
            todo.setCompleted(dto.getCompleted());
        }
        Todo saved = repository.save(todo);
        return toDto(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) throw new NotFoundException("Todo not found with id: " + id);
        repository.deleteById(id);
    }

    public TodoDto toggle(Long id) {
        Todo todo = repository.findById(id).orElseThrow(() -> new NotFoundException("Todo not found with id: " + id));
        todo.setCompleted(!todo.isCompleted());
        return toDto(repository.save(todo));
    }

    private TodoDto toDto(Todo todo) {
        TodoDto dto = new TodoDto();
        dto.setId(todo.getId());
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setCompleted(todo.isCompleted());
        return dto;
    }

}
