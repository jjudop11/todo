package com.example.todo.dto.todo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTodo {
    @NotBlank
    private String title;
    private String description;
}


