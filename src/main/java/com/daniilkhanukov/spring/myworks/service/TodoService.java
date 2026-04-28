package com.daniilkhanukov.spring.myworks.service;

import com.daniilkhanukov.spring.myworks.dto.TodoDto;
import com.daniilkhanukov.spring.myworks.entity.Todo;
import com.daniilkhanukov.spring.myworks.exception.NotFoundException;
import com.daniilkhanukov.spring.myworks.mapper.TodoMapper;
import com.daniilkhanukov.spring.myworks.repository.JdbcTodoRepository;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private static final String TODO_NOT_FOUND = "Todo not found with id: ";

    private final JdbcTodoRepository repository;
    private final TodoMapper mapper;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void initMetrics() {
        Gauge.builder("todos.completed.count", repository, r -> r.countCompleted())
                .description("Number of completed todos")
                .register(meterRegistry);
    }

    @Cacheable(value = "todos", key = "{#completed, #limit, #offset}")
    public List<TodoDto> getAll(Boolean completed, int limit, int offset) {
        List<Todo> list = (completed == null) ? repository.findAll(limit, offset) : repository.findByCompleted(completed, limit, offset);
        return list.stream().map(mapper::toDto).toList();
    }

    public TodoDto getById(Long id) {
        return repository.findById(id).map(mapper::toDto).orElseThrow(() -> new NotFoundException(TODO_NOT_FOUND + id));
    }

    @CacheEvict(value = "todos", allEntries = true)
    public TodoDto create(TodoDto dto) {
        Todo t = mapper.toEntity(dto);
        t.setCompleted(dto.getCompleted() != null && dto.getCompleted());
        Todo saved = repository.save(t);
        return mapper.toDto(saved);
    }

    @CacheEvict(value = "todos", allEntries = true)
    public TodoDto update(Long id, TodoDto dto) {
        Todo existing = repository.findById(id).orElseThrow(() -> new NotFoundException(TODO_NOT_FOUND + id));
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        if (dto.getCompleted() != null) existing.setCompleted(dto.getCompleted());
        repository.save(existing);
        return mapper.toDto(existing);
    }

    @CacheEvict(value = "todos", allEntries = true)
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @CacheEvict(value = "todos", allEntries = true)
    public TodoDto toggle(Long id) {
        Todo t = repository.findById(id).orElseThrow(() -> new NotFoundException(TODO_NOT_FOUND + id));
        t.setCompleted(!t.isCompleted());
        repository.save(t);
        return mapper.toDto(t);
    }
}
