package rut.miit.airportweb.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity(name = "user_entity")
@Table(name = "users", schema = "public")
@Setter
@AllArgsConstructor
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

    public static UserEntityBuilder builder() {
        return new UserEntityBuilder();
    }

    public static final class UserEntityBuilder {

        private UserEntity user = new UserEntity();

        public UserEntityBuilder username(String username) {
            this.user.setUsername(username);
            return this;
        }

        public UserEntityBuilder password(String password) {
            this.user.setPassword(password);
            return this;
        }

        public UserEntityBuilder role(Role role) {
            this.user.setRole(role);
            return this;
        }

        public UserEntityBuilder firstName(String firstName) {
            this.user.setFirstName(firstName);
            return this;
        }

        public UserEntityBuilder lastName(String lastName) {
            this.user.setLastName(lastName);
            return this;
        }

        public UserEntityBuilder createdAt(LocalDateTime createdAt) {
            this.user.setCreatedAt(createdAt);
            return this;
        }

        public UserEntityBuilder passenger(PassengerEntity passenger) {
            this.user.setPassenger(passenger);
            return this;
        }

        public UserEntity build() {
            return this.user;
        }
    }

}
