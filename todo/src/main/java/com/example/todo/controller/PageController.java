package com.example.todo.controller;

import com.example.todo.viewmodel.TodoItemVM;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public PageController(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            var user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                var items = todoRepository.findByUserOrderByCreatedAtDesc(user, org.springframework.data.domain.PageRequest.of(0, 10))
                        .map(t -> TodoItemVM.builder()
                                .id(t.getId())
                                .title(t.getTitle())
                                .completed(t.isCompleted())
                                .priority(t.getPriority())
                                .dueDate(t.getDueDate() == null ? null : t.getDueDate().toString())
                                .build());
                model.addAttribute("todos", items.getContent());
            }
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }
}


