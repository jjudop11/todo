package com.example.todo.viewmodel;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TodoItemVM {
    Long id;
    String title;
    boolean completed;
    String priority;
    String dueDate; // ISO string for simplicity
}


