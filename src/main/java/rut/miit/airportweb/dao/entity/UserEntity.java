package rut.miit.airportweb.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "user_entity")
@Table(name = "users", schema = "public")
@Setter
public class UserEntity {

    private Integer id;
    private String username;
    private String password;
    private Role role;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private PassengerEntity passenger;


    // Constructors
    public UserEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public UserEntity(String username, String password, Role role, String firstName, String lastName, PassengerEntity passenger) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = LocalDateTime.now();
        this.passenger = passenger;
    }

    // Getters and Setters

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() { return id; }

    @NotBlank
    @Column(unique = true, nullable = false, length = 50)
    public String getUsername() { return username; }

    @NotBlank
    @Column(nullable = false, length = 100)
    public String getPassword() { return password; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Role getRole() { return role; }

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 50)
    public String getFirstName() { return firstName; }

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 50)
    public String getLastName() { return lastName; }

    @Column(name = "created_at")
    public LocalDateTime getCreatedAt() { return createdAt; }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    public PassengerEntity getPassenger() { return passenger; }


    public enum Role {
        ADMIN, PASSENGER, AIRPORT_STAFF, BORDER_GUARD, CUSTOMS_OFFICER
    }

}
