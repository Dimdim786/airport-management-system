package rut.miit.airportweb.service;

import rut.miit.airportweb.dto.PassengerCreateDto;
import rut.miit.airportweb.dto.PassengerDto;

import java.util.List;

/**
 * Сервис для управления пассажирами
 */
public interface PassengerService {


    /**
     * Получить пассажира по номеру паспорта
     * @param passportNumber номер паспорта
     * @return Optional с пассажиром
     */
    PassengerDto getPassengerByPassportNumber(String passportNumber);

    /**
     * Получить пассажира по телефону
     * @param phone номер телефона
     * @return Optional с пассажиром
     */
    PassengerDto getPassengerByPhone(String phone);

    /**
     * Получить пассажира по email
     * @param email email адрес
     * @return Optional с пассажиром
     */
    PassengerDto getPassengerByEmail(String email);

    /**
     * Создать нового пассажира
     * @param passengerCreateDto DTO создания пассажира
     * @return созданный пассажир
     */
    PassengerDto createPassenger(PassengerCreateDto passengerCreateDto);

//    /**
//     * Обновить пассажира
//     * @param id идентификатор пассажира
//     * @param passengerUpdateDto DTO обновления пассажира
//     * @return обновленный пассажир
//     */
//    PassengerDto updatePassenger(Integer id, PassengerUpdateDto passengerUpdateDto);

    /**
     * Удалить пассажира
     * @param id идентификатор пассажира
     */
    void deletePassenger(String passportNumber);

    /**
     * Найти всех пассажиров по имени и фамилии
     * @param firstName имя
     * @param lastName фамилия
     * @return список пассажиров
     */
    List<PassengerDto> findAllByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Найти всех пассажиров по имени пользователя
     * @param username имя пользователя
     * @return список пассажиров
     */
    List<PassengerDto> findByUsername(String username);

    /**
     * Обновить статус проверки багажа
     * @param passengerId ID пассажира
     * @param luggageChecked статус проверки багажа
     * @return обновленный пассажир
     */
    PassengerDto updateLuggageStatus(String passengerPassportNumber, boolean luggageChecked);

    /**
     * Проверить паспортные данные пассажира
     * @param passportNumber номер паспорта
     * @return результат проверки
     */
    PassportVerificationResult verifyPassport(String passportNumber);


    /**
     * Результат проверки паспорта
     */
    class PassportVerificationResult {
        private final boolean isValid;
        private final String message;
        private final PassengerDto passenger;

        public PassportVerificationResult(boolean isValid, String message, PassengerDto passenger) {
            this.isValid = isValid;
            this.message = message;
            this.passenger = passenger;
        }

        // Getters
        public boolean isValid() { return isValid; }
        public String getMessage() { return message; }
        public PassengerDto getPassenger() { return passenger; }
    }

}
