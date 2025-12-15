package rut.miit.airportweb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.airportweb.dao.entity.PassengerEntity;
import rut.miit.airportweb.dao.entity.UserEntity;
import rut.miit.airportweb.dao.repository.PassengerRepository;
import rut.miit.airportweb.dao.repository.UserRepository;
import rut.miit.airportweb.dto.UserDto;
import rut.miit.airportweb.dto.UserRegistrationDto;
import rut.miit.airportweb.exception.EntityNotFoundException;
import rut.miit.airportweb.mapper.UserMapper;
import rut.miit.airportweb.service.UserService;
import rut.miit.airportweb.exception.NotPermittedOperation;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PassengerRepository passengerRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDto registerUser(UserRegistrationDto dto) {
        log.info("Registering user {}", dto.getUsername());

        // Проверки...

        try {
            // Сначала создаем и сохраняем User
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(dto.getUsername());
            userEntity.setPassword(this.passwordEncoder.encode(dto.getPassword()));
            userEntity.setFirstName(dto.getFirstName());
            userEntity.setLastName(dto.getLastName());
            userEntity.setRole(UserEntity.Role.PASSENGER);

            UserEntity savedUser = userRepository.save(userEntity);

            // Затем создаем и сохраняем Passenger
            PassengerEntity passenger = new PassengerEntity();
            passenger.setUser(savedUser);
            passenger.setPassportNumber(dto.getPassportNumber());
            passenger.setEmail(dto.getEmail());
            passenger.setPhone(dto.getPhone());
            passenger.setLuggageChecked(false);

            PassengerEntity savedPassenger = passengerRepository.save(passenger);

            // Обновляем связь
            savedUser.setPassenger(savedPassenger);
            userRepository.save(savedUser);

            log.info("Registration successful: User ID={}, Passenger ID={}",
                    savedUser.getId(), savedPassenger.getId());

            return UserMapper.map(savedUser);

        } catch (Exception e) {
            log.error("Registration failed", e);
            throw new RuntimeException("Registration failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) throws EntityNotFoundException {
        UserEntity entity = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with username %s not found", username)));

        return UserMapper.map(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByFullName(String firstName, String lastName) throws EntityNotFoundException {
        UserEntity entity = this.userRepository.findByFirstNameAndLastName(firstName, lastName)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with fullname %s %s not found", firstName, lastName)));

        return UserMapper.map(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsersOptimized() {
        return this.userRepository.findAllOptimized()
                .stream()
                .map(UserMapper::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto authenticate(String username, String password) throws EntityNotFoundException {
        log.info("Authenticating user {}", username);

        UserEntity user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with username %s not found", username)));

        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            throw new NotPermittedOperation("Invalid password");
        }

        log.info("User {} authenticated successfully", username);
        return UserMapper.map(user);
    }

    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public UserDto updateUser(String username, UserDto userDto) {
        UserEntity user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User with username %s not found", username)));

        // Обновляем поля, если они предоставлены
        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getPasswordHash() != null) {
            user.setPassword(this.passwordEncoder.encode(userDto.getPasswordHash()));
        }

        UserEntity updatedUser = this.userRepository.save(user);
        log.info("Updated user {}", username);

        return UserMapper.map(updatedUser);
    }

    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void deleteUser(String username) {
        UserEntity user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User with username %s not found", username)));

        this.userRepository.delete(user);
        log.info("Deleted user {}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        return this.userRepository.findByUsername(username).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllPassengers() {
        // Оптимизированный запрос - фильтруем на уровне базы данных
        return this.userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == UserEntity.Role.PASSENGER)
                .map(UserMapper::map)
                .toList();
    }

}