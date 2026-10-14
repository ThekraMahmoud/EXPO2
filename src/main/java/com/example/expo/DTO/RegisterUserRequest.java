package com.example.expo.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(@NotBlank @Size(max=100) String username, @NotBlank @Email String email,
                                  @NotBlank @Pattern(regexp="^[0-9]{10}$") String phone,
                                  @NotBlank @Size(min=8,max=72) String password) {}
