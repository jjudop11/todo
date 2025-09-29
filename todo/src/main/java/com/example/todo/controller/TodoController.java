package com.example.todo.controller;

import com.example.todo.domain.Todo;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/todos")
@Validated
public class TodoController {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public TodoController(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(todoRepository.findByUserOrderByCreatedAtDesc(user));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateTodo req, Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        var todo = Todo.builder()
                .user(user)
                .title(req.getTitle())
                .description(req.getDescription())
                .completed(false)
                .build();
        todoRepository.save(todo);
        return ResponseEntity.ok(Map.of("id", todo.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateTodo req, Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        var todo = todoRepository.findById(id).orElseThrow();
        if (!todo.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        if (req.getTitle() != null) todo.setTitle(req.getTitle());
        if (req.getDescription() != null) todo.setDescription(req.getDescription());
        if (req.getCompleted() != null) todo.setCompleted(req.getCompleted());
        todoRepository.save(todo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        var todo = todoRepository.findById(id).orElseThrow();
        if (!todo.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        todoRepository.delete(todo);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class CreateTodo {
        @NotBlank
        private String title;
        private String description;
    }

    @Data
    public static class UpdateTodo {
        private String title;
        private String description;
        private Boolean completed;
    }
}


