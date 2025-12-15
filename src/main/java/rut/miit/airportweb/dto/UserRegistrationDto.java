package rut.miit.airportweb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserRegistrationDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String confirmPassword;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone cannot be blank")
    @NotNull(message = "Phone cannot be null")
    private String phone;

    @NotNull(message = "Role cannot be null")
    @NotBlank(message = "Role cannot be blank")
    private String role;

    @NotBlank(message = "Passport number cannot be blank")
    @NotNull(message = "Passport number cannot be null")
    private String passportNumber;

    // Удаляем ручные геттеры/сеттеры и оставляем только конструктор по умолчанию
    public UserRegistrationDto() {}
}