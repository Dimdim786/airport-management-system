package rut.miit.airportweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public class TicketDto {

    private Integer id;

    @NotNull(message = "Flight ID is required")
    private Integer flightId;

    @NotNull(message = "Passenger ID is required")
    private Integer passengerId;

    @NotBlank(message = "Seat number is required")
    private String seatNumber;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private String ticketNumber;
    private String status;
    private LocalDateTime bookingDate;

    // Additional fields for display
    private String flightNumber;
    private String passengerName;
    private String passportNumber;

    // Constructors
    public TicketDto() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getFlightId() { return flightId; }
    public void setFlightId(Integer flightId) { this.flightId = flightId; }

    public Integer getPassengerId() { return passengerId; }
    public void setPassengerId(Integer passengerId) { this.passengerId = passengerId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }

}
