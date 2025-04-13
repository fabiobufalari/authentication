package com.constructionhub.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 3, max = 50, message = "{validation.size.username}")
    private String username;

    @NotBlank(message = "{validation.notBlank}")
    @Email(message = "{validation.email}")
    private String email;

    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 8, message = "{validation.minLength}")
    private String password;

    private String firstName;

    private String lastName;
}