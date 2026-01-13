package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
@Getter
@Setter
@NoArgsConstructor
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Todo(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
