package com.daniilkhanukov.spring.myworks.repository;

import com.daniilkhanukov.spring.myworks.entity.Todo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class  JdbcTodoRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Todo> mapper = (rs, rowNum) -> {
        Todo t =new Todo();
        t.setId(rs.getLong("id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setCompleted(rs.getBoolean("completed"));
        t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return t;
    };

    public JdbcTodoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Todo> findAll(int limit, int offset) {
        String sql = "SELECT * FROM todos ORDER BY id LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, mapper, limit, offset);
    }

    public Optional<Todo> findById(Long id) {
        String sql = "SELECT * FROM todos WHERE id = ?";
        List<Todo> list = jdbcTemplate.query(sql, mapper, id);
        return list.stream().findFirst();
    }

    public List<Todo> findByCompleted(boolean completed, int limit, int offset) {
        String sql = "SELECT * FROM todos WHERE completed = ? ORDER BY id LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, mapper, completed, limit, offset);
    }

    public Todo save(Todo t) {
        if (t.getId() == null) {
            String sql = """
                INSERT INTO todos (title, description, completed)
                VALUES (?, ?, ?)
                RETURNING id
                """;

            Long id = jdbcTemplate.queryForObject(
                    sql,
                    Long.class,
                    t.getTitle(),
                    t.getDescription(),
                    t.isCompleted()
            );

            t.setId(id);
            return t;
        } else {
            String sql = "UPDATE todos SET title = ?, description = ?, completed = ? WHERE id = ?";
            jdbcTemplate.update(sql, t.getTitle(), t.getDescription(), t.isCompleted(), t.getId());
            return t;
        }
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM todos WHERE id = ?", id);
    }

    public long countCompleted() {
        Long result = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM todos WHERE completed = true",
                Long.class
        );
        return result != null ? result : 0L;
    }
}
