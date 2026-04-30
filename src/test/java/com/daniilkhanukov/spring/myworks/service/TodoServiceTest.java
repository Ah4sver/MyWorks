package com.daniilkhanukov.spring.myworks.service;

import com.daniilkhanukov.spring.myworks.dto.TodoDto;
import com.daniilkhanukov.spring.myworks.entity.Todo;
import com.daniilkhanukov.spring.myworks.exception.NotFoundException;
import com.daniilkhanukov.spring.myworks.mapper.TodoMapper;
import com.daniilkhanukov.spring.myworks.repository.JdbcTodoRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private JdbcTodoRepository repository;

    @Mock
    private TodoMapper mapper;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private TodoService service;

    @Test
    void getAllWhenCompletedIsNullShouldUseFindAll() {
        Todo todo = todo(1L, "Title", "Description", false);
        TodoDto dto = todoDto(1L, "Title", "Description", false);

        when(repository.findAll(10, 0)).thenReturn(List.of(todo));
        when(mapper.toDto(todo)).thenReturn(dto);

        List<TodoDto> result = service.getAll(null, 10, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(repository).findAll(10, 0);
        verify(repository, never()).findByCompleted(anyBoolean(), anyInt(), anyInt());
    }

    @Test
    void getAllWhenCompletedIsNotNullShouldUseFindByCompleted() {
        Todo todo = todo(1L, "Title", "Description", true);
        TodoDto dto = todoDto(1L, "Title", "Description", true);

        when(repository.findByCompleted(true, 10, 0)).thenReturn(List.of(todo));
        when(mapper.toDto(todo)).thenReturn(dto);

        List<TodoDto> result = service.getAll(true, 10, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompleted()).isTrue();
        verify(repository).findByCompleted(true, 10, 0);
        verify(repository, never()).findAll(anyInt(), anyInt());
    }

    @Test
    void getByIdShouldReturnTodoDto() {
        Todo todo = todo(1L, "Title", "Description", false);
        TodoDto dto = todoDto(1L, "Title", "Description", false);

        when(repository.findById(1L)).thenReturn(Optional.of(todo));
        when(mapper.toDto(todo)).thenReturn(dto);

        TodoDto result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Title");
    }

    @Test
    void getByIdShouldThrowWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Todo not found with id: 99");
    }

    @Test
    void createShouldForceCompletedFalseWhenDtoCompletedIsNull() {
        TodoDto input = todoDto(null, "New title", "New description", null);
        Todo entity = todo(null, "New title", "New description", true);
        Todo saved = todo(1L, "New title", "New description", false);
        TodoDto output = todoDto(1L, "New title", "New description", false);

        when(mapper.toEntity(input)).thenReturn(entity);
        when(repository.save(any(Todo.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(output);

        TodoDto result = service.create(input);

        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).save(argThat(t -> !t.isCompleted()));
    }

    @Test
    void createShouldRespectCompletedTrue() {
        TodoDto input = todoDto(null, "New title", "New description", true);
        Todo entity = todo(null, "New title", "New description", false);
        Todo saved = todo(1L, "New title", "New description", true);
        TodoDto output = todoDto(1L, "New title", "New description", true);

        when(mapper.toEntity(input)).thenReturn(entity);
        when(repository.save(any(Todo.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(output);

        TodoDto result = service.create(input);

        assertThat(result.getCompleted()).isTrue();
        verify(repository).save(argThat(Todo::isCompleted));
    }

    @Test
    void updateShouldChangeTitleDescriptionAndKeepCompletedWhenDtoCompletedIsNull() {
        Todo existing = todo(1L, "Old title", "Old description", true);
        TodoDto input = todoDto(null, "New title", "New description", null);
        TodoDto output = todoDto(1L, "New title", "New description", true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(output);

        TodoDto result = service.update(1L, input);

        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getCompleted()).isTrue();
        verify(repository).save(existing);
    }

    @Test
    void updateShouldSetCompletedWhenDtoCompletedIsProvided() {
        Todo existing = todo(1L, "Old title", "Old description", true);
        TodoDto input = todoDto(null, "New title", "New description", false);
        TodoDto output = todoDto(1L, "New title", "New description", false);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(output);

        TodoDto result = service.update(1L, input);

        assertThat(result.getCompleted()).isFalse();
        verify(repository).save(argThat(t -> !t.isCompleted()));
    }

    @Test
    void updateShouldThrowWhenTodoMissing() {
        TodoDto input = todoDto(null, "New title", "New description", true);

        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(10L, input))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Todo not found with id: 10");
    }

    @Test
    void deleteShouldCallRepository() {
        service.delete(5L);

        verify(repository).deleteById(5L);
    }

    @Test
    void toggleShouldInvertCompletedAndSave() {
        Todo existing = todo(1L, "Title", "Description", false);
        TodoDto output = todoDto(1L, "Title", "Description", true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(output);

        TodoDto result = service.toggle(1L);

        assertThat(result.getCompleted()).isTrue();
        verify(repository).save(argThat(Todo::isCompleted));
    }

    private Todo todo(Long id, String title, String description, boolean completed) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setCompleted(completed);
        return todo;
    }

    private TodoDto todoDto(Long id, String title, String description, Boolean completed) {
        TodoDto dto = new TodoDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setCompleted(completed);
        return dto;
    }
}
