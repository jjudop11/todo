package com.example.todo.exception;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiError {
    int status;
    String code;
    String message;
}


