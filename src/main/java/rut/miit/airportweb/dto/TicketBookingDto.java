package rut.miit.airportweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TicketBookingDto {

    @NotBlank(message = "Flight number is required")
    private String flightNumber;

    @NotBlank(message = "Seat number is required")
    private String seatNumber;

    @NotBlank(message = "Passport number is required")
    private String passportNumber;

    @NotBlank(message = "Passenger name is required")
    private String passengerName;

    // Дополнительные поля, если нужны
    private BigDecimal price; // Можно задать фиксированную цену или рассчитать

    // Для генерации номера билета
    private String ticketNumber; // Можно генерировать автоматически

    public TicketBookingDto() {}
}