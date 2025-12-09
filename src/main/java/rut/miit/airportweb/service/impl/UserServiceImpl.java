package rut.miit.airportweb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.airportweb.dao.entity.UserEntity;
import rut.miit.airportweb.dao.repository.UserRepository;
import rut.miit.airportweb.dto.UserDto;
import rut.miit.airportweb.dto.UserRegistrationDto;
import rut.miit.airportweb.exception.EntityAlreadyExistsException;
import rut.miit.airportweb.exception.EntityNotFoundException;
import rut.miit.airportweb.mapper.UserMapper;
import rut.miit.airportweb.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public UserDto registerUser(UserRegistrationDto dto) {
        log.info("Registering user {}", dto.getEmail());
        if (this.userRepository.findByUsername(dto.getUsername()).isPresent()) {
            log.info("User already exists");
            throw new EntityAlreadyExistsException(String.format("User with username %s already exists", dto.getUsername()));
        }

        UserEntity entity = UserMapper.map(dto);
        entity.setPassword(this.passwordEncoder.encode(entity.getPassword()));

        UserEntity savedUser = this.userRepository.save(entity);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return UserMapper.map(savedUser);
    }

    @Override
    public UserDto getUserByUsername(String username) throws EntityNotFoundException {
        UserEntity entity = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with username %s not found", username)));

        return UserMapper.map(entity);
    }

    @Override
    public UserDto getUserByFullName(String firstName, String lastName) throws EntityNotFoundException {
        UserEntity entity = this.userRepository.findByFirstNameAndLastName(firstName, lastName)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with fullname %s %s not found", firstName, lastName)));

        return UserMapper.map(entity);
    }

    @Override
    public List<UserDto> getAllUsersOptimized() {
        return this.userRepository.findAll()
                .stream()
                .map(UserMapper::map)
                .toList();
    }

    // Пока что без реализации
    @Override
    public UserDto authenticate(String username, String password) throws EntityNotFoundException {
        log.info("Authenticating user {}", username);

        return this.userRepository.findByUsername(username)
                .filter(userEntity -> this.passwordEncoder.matches(password, userEntity.getPassword()))
                .map(UserMapper::map)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with username %s not found", username)));
    }


    // Пока без реализации
    @Override
    public UserDto updateUser(String username, UserDto userDto) {
        log.info("Метод без реализации");
        return null;
    }

    @Override
    public void deleteUser(String username) {
        this.userRepository.findByUsername(username)
                .ifPresentOrElse(
                        this.userRepository::delete,
                        () -> {
                            throw new IllegalStateException("No user to delete");
                        }
                );
    }

    @Override
    public boolean userExists(String username) {
        return this.userRepository.findByUsername(username).isPresent();
    }

    @Override
    public List<UserDto> getAllPassengers() {
        return this.getAllUsersOptimized()
                .stream()
                .filter(user -> user.getRole().toString().equals(UserEntity.Role.PASSENGER.toString()))
                .toList();
    }
}
