package rut.miit.airportweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class PassengerCreateDto {

    @NotNull(message = "Owner username cannot be null")
    @NotBlank(message = "Owner username cannot be blank")
    private String ownerUsername;

    @NotNull(message = "Passport number cannot be null")
    @NotBlank(message = "Passport number cannot be blank")
    private String passportNumber;

    @NotNull(message = "Phone number cannot be null")
    @NotBlank(message = "Phone number cannot be blank")
    private String phone;

    @NotNull(message = "Email cannot be null")
    @NotBlank(message = "Email cannot be blank")
    private String email;
}
