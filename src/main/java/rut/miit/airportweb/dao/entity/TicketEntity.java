package rut.miit.airportweb.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "ticket_entity")
@Table(name = "tickets", schema = "public")
@Setter
public class TicketEntity {

    private Integer id;
    private FlightEntity flight;
    private PassengerEntity passenger;
    private String seatNumber;
    private BigDecimal price;
    private String ticketNumber;
    private TicketStatus status;
    private LocalDateTime bookingDate;
    private BoardingPassEntity boardingPass;


    // Constructors, Getters and Setters
    public TicketEntity() {
        this.status = TicketStatus.BOOKED;
        this.bookingDate = LocalDateTime.now();
    }

    @Builder
    public TicketEntity(FlightEntity flight, PassengerEntity passenger, String seatNumber, BigDecimal price, String ticketNumber, TicketStatus status, LocalDateTime bookingDate, BoardingPassEntity boardingPass) {
        this.flight = flight;
        this.passenger = passenger;
        this.seatNumber = seatNumber;
        this.price = price;
        this.ticketNumber = ticketNumber;
        this.status = TicketStatus.BOOKED;
        this.bookingDate = LocalDateTime.now();
        this.boardingPass = boardingPass;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() { return id; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    public FlightEntity getFlight() { return flight; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    public PassengerEntity getPassenger() { return passenger; }

    @NotBlank
    @Column(name = "seat_number", nullable = false)
    public String getSeatNumber() { return seatNumber; }

    @Positive
    @Column(nullable = false, name = "price", precision = 10, scale = 2)
    public BigDecimal getPrice() { return price; }

    @NotBlank
    @Column(name = "ticket_number", unique = true, nullable = false)
    public String getTicketNumber() { return ticketNumber; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TicketStatus getStatus() { return status; }

    @Column(name = "booking_date")
    public LocalDateTime getBookingDate() { return bookingDate; }

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL)
    public BoardingPassEntity getBoardingPass() { return boardingPass; }


    public enum TicketStatus {
        BOOKED, CHECKED_IN, BOARDED
    }
}
