package com.daniilkhanukov.spring.myworks.repository;

import com.daniilkhanukov.spring.myworks.entity.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcTodoRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private JdbcTodoRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        repository = new JdbcTodoRepository(jdbcTemplate);
    }

    @Test
    void findAllShouldMapRows() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    RowMapper<Todo> rowMapper = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);

                    when(rs.getLong("id")).thenReturn(1L);
                    when(rs.getString("title")).thenReturn("Title");
                    when(rs.getString("description")).thenReturn("Description");
                    when(rs.getBoolean("completed")).thenReturn(true);
                    when(rs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2024, 1, 1, 10, 0)));

                    return List.of(rowMapper.mapRow(rs, 0));
                });

        List<Todo> result = repository.findAll(10, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Title");
        assertThat(result.get(0).isCompleted()).isTrue();
    }

    @Test
    void findByIdShouldReturnOptionalValue() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1L)))
                .thenAnswer(invocation -> {
                    RowMapper<Todo> rowMapper = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);

                    when(rs.getLong("id")).thenReturn(1L);
                    when(rs.getString("title")).thenReturn("Title");
                    when(rs.getString("description")).thenReturn("Description");
                    when(rs.getBoolean("completed")).thenReturn(false);
                    when(rs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2024, 1, 1, 10, 0)));

                    return List.of(rowMapper.mapRow(rs, 0));
                });

        Optional<Todo> result = repository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void findByCompletedShouldReturnMappedRows() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyBoolean(), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    RowMapper<Todo> rowMapper = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);

                    when(rs.getLong("id")).thenReturn(2L);
                    when(rs.getString("title")).thenReturn("Done");
                    when(rs.getString("description")).thenReturn("Desc");
                    when(rs.getBoolean("completed")).thenReturn(true);
                    when(rs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2024, 1, 1, 10, 0)));

                    return List.of(rowMapper.mapRow(rs, 0));
                });

        List<Todo> result = repository.findByCompleted(true, 10, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Done");
        assertThat(result.get(0).isCompleted()).isTrue();
    }

    @Test
    void saveShouldInsertAndSetGeneratedId() {
        Todo todo = new Todo();
        todo.setTitle("New title");
        todo.setDescription("New description");
        todo.setCompleted(false);

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Long.class),
                any(),
                any(),
                any()
        )).thenReturn(100L);

        Todo saved = repository.save(todo);

        assertThat(saved.getId()).isEqualTo(100L);
        verify(jdbcTemplate).queryForObject(
                contains("INSERT INTO todos"),
                eq(Long.class),
                eq("New title"),
                eq("New description"),
                eq(false)
        );
    }

    @Test
    void saveShouldUpdateExistingTodo() {
        Todo todo = new Todo();
        todo.setId(10L);
        todo.setTitle("Updated title");
        todo.setDescription("Updated description");
        todo.setCompleted(true);

        Todo saved = repository.save(todo);

        assertThat(saved.getId()).isEqualTo(10L);
        verify(jdbcTemplate).update(
                contains("UPDATE todos SET title = ?, description = ?, completed = ? WHERE id = ?"),
                eq("Updated title"),
                eq("Updated description"),
                eq(true),
                eq(10L)
        );
    }

    @Test
    void deleteByIdShouldDelegateToJdbcTemplate() {
        repository.deleteById(55L);

        verify(jdbcTemplate).update("DELETE FROM todos WHERE id = ?", 55L);
    }

    @Test
    void countCompletedShouldReturnValue() {
        when(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM todos WHERE completed = true",
                Long.class
        )).thenReturn(7L);

        long result = repository.countCompleted();

        assertThat(result).isEqualTo(7L);
    }

    @Test
    void countCompletedShouldReturnZeroWhenNull() {
        when(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM todos WHERE completed = true",
                Long.class
        )).thenReturn(null);

        long result = repository.countCompleted();

        assertThat(result).isZero();
    }
}
