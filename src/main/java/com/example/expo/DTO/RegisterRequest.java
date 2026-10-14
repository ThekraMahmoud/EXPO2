package com.example.expo.DTO;
import jakarta.validation.constraints.*; import lombok.Getter; import lombok.Setter;
@Getter @Setter public class RegisterRequest { @NotBlank @Size(max=100) private String username; @NotBlank @Email private String email; @NotBlank @Pattern(regexp="^[0-9]{10}$") private String phone; @NotBlank @Size(min=8,max=72) private String password; }
