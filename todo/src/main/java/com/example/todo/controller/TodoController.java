package com.example.todo.controller;

import com.example.todo.domain.Todo;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import com.example.todo.dto.todo.CreateTodo;
import com.example.todo.dto.todo.UpdateTodo;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<?> list(Authentication authentication,
                                  @RequestParam(required = false) Boolean completed,
                                  @RequestParam(required = false) String priority,
                                  @RequestParam(required = false) String tag,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Pageable pageable = PageRequest.of(page, size);
        var result = todoRepository.search(user, completed, priority, tag, pageable);
        return ResponseEntity.ok(result);
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

    
}


