package rut.miit.airportweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import rut.miit.airportweb.dao.entity.UserEntity;

import java.time.LocalDateTime;

// TODO: Если кто-то захочет переписать на норм getter и setter, то перепишите
@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDto {

    private Integer id;

    @Size(max = 50, message = "Username cannot be longer than 50 symbols")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Size(max = 100, message = "Password hash cannot be longer than 100 symbols")
    @NotBlank(message = "Password hash cannot be blank")
    private String passwordHash;

    @NotNull(message = "Role cannot be null")
    private UserEntity.Role role;

    @Size(max = 50, message = "First name cannot be longer than 50 symbols")
    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotNull(message = "Created at date cannot be null")
    private LocalDateTime createdAt;

    @NotNull(message = "Passenger id cannot null")
    private String passengerId;

}
