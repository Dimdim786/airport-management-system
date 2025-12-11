package rut.miit.airportweb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.airportweb.dao.entity.PassengerEntity;
import rut.miit.airportweb.dao.entity.UserEntity;
import rut.miit.airportweb.dao.repository.PassengerRepository;
import rut.miit.airportweb.dao.repository.UserRepository;
import rut.miit.airportweb.dto.PassengerCreateDto;
import rut.miit.airportweb.dto.PassengerDto;
import rut.miit.airportweb.exception.EntityNotFoundException;
import rut.miit.airportweb.exception.NotPermittedOperation;
import rut.miit.airportweb.mapper.PassengerMapper;
import rut.miit.airportweb.service.PassengerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PassengerServiceImpl implements PassengerService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\\\+?[0-9]{10,15}$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Z0-9]{6,10}$");


    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;

    @Override
    public PassengerDto getPassengerByPassportNumber(String passportNumber) {
        PassengerEntity passenger = this.passengerRepository.findByPassportNumber(passportNumber)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Passenger with passport number %S not found", passportNumber)));

        return PassengerMapper.map(passenger);
    }

    @Override
    public PassengerDto getPassengerByPhone(String phone) {
        PassengerEntity passenger = this.passengerRepository.findByPhone(phone)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Passenger with phone %s not found", phone)));

        return PassengerMapper.map(passenger);
    }

    @Override
    public PassengerDto getPassengerByEmail(String email) {
        PassengerEntity passenger = this.passengerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Passenger with email %S not found", email)));

        return PassengerMapper.map(passenger);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PassengerDto createPassenger(PassengerCreateDto passengerCreateDto) {
        PassengerEntity passengerEntity = PassengerMapper.map(passengerCreateDto);

        UserEntity owner = this.userRepository.findByUsername(passengerCreateDto.getOwnerUsername())
                        .orElseThrow(() -> new EntityNotFoundException(String.format("User with username %s not found", passengerCreateDto.getOwnerUsername())));

        passengerEntity.setUser(owner);
        passengerEntity.setTickets(new ArrayList<>());

        return PassengerMapper.map(passengerEntity);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deletePassenger(String passportNumber) {
        this.passengerRepository.findByPassportNumber(passportNumber)
                .ifPresentOrElse(
                        this.passengerRepository::delete,
                        () -> {
                            throw new EntityNotFoundException(String.format("Passenger with passport number %s not found", passportNumber));
                        }
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassengerDto> findAllByFirstNameAndLastName(String firstName, String lastName) {
        return this.passengerRepository.findAllByFirstNameAndLastName(firstName, lastName)
                .stream()
                .map(PassengerMapper::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassengerDto> findByUsername(String username) {
        return this.passengerRepository.findByUsername(username)
                .stream()
                .map(PassengerMapper::map)
                .toList();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PassengerDto updateLuggageStatus(String passengerPassportNumber, boolean luggageChecked) {
        return this.passengerRepository.updateLuggageStatus(passengerPassportNumber, luggageChecked)
                .map(PassengerMapper::map)
                .orElseThrow(
                        () -> new EntityNotFoundException(String.format("Passenger with passport number %s not found", passengerPassportNumber))
                );
    }

    @Override
    @Transactional(readOnly = true)
    public PassportVerificationResult verifyPassport(String passportNumber) {
        Optional<PassengerEntity> optPassenger = this.passengerRepository.findByPassportNumber(passportNumber);

        if (optPassenger.isEmpty()) {
            return new PassportVerificationResult(
                    false,
                    "No passenger with this passport",
                    null
            );
        }

        PassengerEntity passenger = optPassenger.get();

        if (!validatePassport(passportNumber)) {
            return new PassportVerificationResult(
                    false,
                    "Invalid passport format",
                    null
            );
        }

        if (!validatePhone(passenger.getPhone())) {
            return new PassportVerificationResult(
                    false,
                    "Invalid phone format",
                    null
            );
        }

        if (!validateEmail(passenger.getPhone())) {
            return new PassportVerificationResult(
                    false,
                    "Invalid email format",
                    null
            );
        }

        // Если все проверки прошли
        return new PassportVerificationResult(
                true,
                "Valid",
                PassengerMapper.map(passenger)
        );
    }

    @Deprecated(forRemoval = true)
    private void validatePassengerData(PassengerCreateDto createDto) {
        String passportNumber = createDto.getPassportNumber();
        String email = createDto.getEmail();;
        String phone = createDto.getPhone();

        if (!validatePassport(passportNumber)) {
            throw new NotPermittedOperation("Invalid passport number provided");
        }

        if (!validateEmail(email)) {
            throw new NotPermittedOperation("Invalid email format provided");
        }

        if (!validatePhone(phone)) {
            throw new NotPermittedOperation("Invalid phone format provided");
        }
    }

    private boolean validateEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean validatePassport(String passport) {
        return PASSPORT_PATTERN.matcher(passport).matches();
    }

    private boolean validatePhone(String phoneNumber) {
        return EMAIL_PATTERN.matcher(phoneNumber).matches();
    }
}
