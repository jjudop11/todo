package com.example.todo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String refreshToken;
}


