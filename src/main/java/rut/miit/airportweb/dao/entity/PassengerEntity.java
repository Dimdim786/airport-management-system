package rut.miit.airportweb.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "passenger_entity")
@Table(name = "passengers", schema = "public")
@Setter
public class PassengerEntity {

    private Integer id;
    private UserEntity user;
    private String passportNumber;
    private String phone;
    private String email;
    private Boolean luggageChecked = false;
    private List<TicketEntity> tickets = new ArrayList<>();

    // Constructors, Getters and Setters
    public PassengerEntity() {}

    @Builder
    public PassengerEntity(UserEntity user, String passportNumber, String phone, String email) {
        this.user = user;
        this.passportNumber = passportNumber;
        this.phone = phone;
        this.email = email;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() { return id; }

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    public UserEntity getUser() { return user; }

    @Column(name = "passport_number", unique = true, nullable = false)
    public String getPassportNumber() { return passportNumber; }

    @Column(name = "phone")
    public String getPhone() { return phone; }

    @Email
    @Column(name = "email")
    public String getEmail() { return email; }

    @Column(name = "luggage_checked")
    public Boolean getLuggageChecked() { return luggageChecked; }

    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL)
    public List<TicketEntity> getTickets() { return this.tickets; }

}
