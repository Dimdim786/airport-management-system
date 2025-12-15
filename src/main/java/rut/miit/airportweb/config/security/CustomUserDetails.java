package rut.miit.airportweb.config.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import rut.miit.airportweb.dao.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Этот класс используется как раз в {@link rut.miit.airportweb.config.security.CustomUserDetailsService}
 * для того, чтобы в {@link rut.miit.airportweb.config.security.CustomUserDetailsService} перевести в объект
 * из базы данных в него и вернуть там же.
 * Здесь должны быть все те же поля, что и у пользователя в базе данных.
 * Ознакомиться с пользователями можно в {@link rut.miit.airportweb.dao.entity.UserEntity} и однопакетных классах
 * */
@Builder
@AllArgsConstructor
@Getter
@Setter
public class CustomUserDetails implements UserDetails {

    private Integer id;
    private String username;
    private String password;
    private UserEntity.Role role;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
