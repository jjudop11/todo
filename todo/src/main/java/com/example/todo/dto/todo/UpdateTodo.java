package com.example.todo.dto.todo;

import lombok.Data;

@Data
public class UpdateTodo {
    private String title;
    private String description;
    private Boolean completed;
}


