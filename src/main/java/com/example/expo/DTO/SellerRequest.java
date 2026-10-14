package com.example.expo.DTO;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerRequest {

    @NotBlank
    @Size(max = 150)
    private String business_name;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$")
    private String phone;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String commercial_register;
}

//المعلومات الي مايحتاج المستخدم يدخلها
// id
// verification_status
// status
// created_at
// updated_at