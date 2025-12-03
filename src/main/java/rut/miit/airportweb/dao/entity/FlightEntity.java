package rut.miit.airportweb.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "flight_entity")
@Table(name = "flights", schema = "public")
@Setter
public class FlightEntity {

    private Integer id;
    private String flightNumber;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private FlightStatus status;
    private UserEntity createdBy;
    private List<TicketEntity> tickets = new ArrayList<>();

    public enum FlightStatus {
        SCHEDULED, BOARDING, DEPARTED, ARRIVED
    }

    // Constructors, Getters and Setters
    public FlightEntity() {
        this.status = FlightStatus.SCHEDULED;
    }

    public FlightEntity(String flightNumber,
                        String departureCity,
                        String arrivalCity,
                        LocalDateTime departureTime,
                        LocalDateTime arrivalTime,
                        Integer totalSeats,
                        Integer availableSeats,
                        FlightStatus status,
                        UserEntity createdBy)
    {
        this.flightNumber = flightNumber;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.status = status;
        this.createdBy = createdBy;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() { return id; }

    @NotBlank
    @Column(name = "flight_number", unique = true, nullable = false)
    public String getFlightNumber() { return flightNumber; }

    @NotBlank
    @Column(name = "departure_city", nullable = false)
    public String getDepartureCity() { return departureCity; }

    @NotBlank
    @Column(name = "arrival_city", nullable = false)
    public String getArrivalCity() { return arrivalCity; }

    @NotNull
    @Column(name = "departure_time", nullable = false)
    public LocalDateTime getDepartureTime() { return departureTime; }

    @NotNull
    @Column(name = "arrival_time", nullable = false)
    public LocalDateTime getArrivalTime() { return arrivalTime; }

    @Positive
    @Column(name = "total_seats", nullable = false)
    public Integer getTotalSeats() { return totalSeats; }

    @Positive
    @Column(name = "available_seats", nullable = false)
    public Integer getAvailableSeats() { return availableSeats; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public FlightStatus getStatus() { return status; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    public UserEntity getCreatedBy() { return createdBy; }

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL)
    public List<TicketEntity> getTickets() { return tickets; }

    public static FlightEntityBuilder builder() {
        return new FlightEntityBuilder();
    }

    public static class FlightEntityBuilder {
        private String flightNumber;
        private String departureCity;
        private String arrivalCity;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private Integer totalSeats;
        private Integer availableSeats;
        private FlightStatus status;
        private UserEntity createdBy;

        public FlightEntityBuilder flightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        public FlightEntityBuilder departureCity(String departureCity) {
            this.departureCity = departureCity;
            return this;
        }

        public FlightEntityBuilder arrivalCity(String arrivalCity) {
            this.arrivalCity = arrivalCity;
            return this;
        }

        public FlightEntityBuilder departureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        public FlightEntityBuilder arrivalTime(LocalDateTime arrivalTime) {
            this.arrivalTime = arrivalTime;
            return this;
        }

        public FlightEntityBuilder totalSeats(Integer totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        public FlightEntityBuilder availableSeats(Integer availableSeats) {
            this.availableSeats = availableSeats;
            return this;
        }

        public FlightEntityBuilder status(FlightStatus status) {
            this.status = status;
            return this;
        }

        public FlightEntityBuilder createdBy(UserEntity createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public FlightEntity build() {
            return new FlightEntity(
                    this.flightNumber,
                    this.departureCity,
                    this.arrivalCity,
                    this.departureTime,
                    this.arrivalTime,
                    this.totalSeats,
                    this.availableSeats,
                    this.status,
                    this.createdBy
            );
        }
    }

}
