package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.enums.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(

        String userName,
        @NotBlank(message = "email is required")
        @Email(message = "invalid email format")
        @Size(max = 255, message = "email must be less than 255 characters")
        String email,
        @NotBlank(message = "password is required")
        @Size(min = 8, max = 100, message = "password must be 8-100 characters")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain at least one letter and one number"
        )
        String password


) {


}
