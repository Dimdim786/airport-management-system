package rut.miit.airportweb.service;

import rut.miit.airportweb.dto.FlightCreateDto;
import rut.miit.airportweb.dto.FlightDto;

import java.util.List;

public interface FlightService extends CommonService {

    /**
     * Получить рейс по номеру рейса
     * @param flightNumber номер рейса
     * @return Optional с рейсом
     */
    FlightDto getFlightByNumber(String flightNumber);

    /**
     * Создать новый рейс
     * @param flightCreateDto DTO создания рейса
     * @param createdByUserId ID создателя
     * @return созданный рейс
     */
    FlightDto createFlight(FlightCreateDto flightCreateDto, String createdByUserUsername);

    // Пока реализовывать не надо, потом напишу как надо будет
//    /**
//     * Обновить рейс
//     * @param id идентификатор рейса
//     * @param flightUpdateDto DTO обновления рейса
//     * @return обновленный рейс
//     */
//    FlightDto updateFlight(Integer id, FlightUpdateDto flightUpdateDto);

    /**
     * Удалить рейс
     * @param flightNumber идентификатор рейса
     */
    void deleteFlight(String flightNumber);

    /**
     * Найти рейсы по городам отправления и назначения
     * @param departureCity город отправления
     * @param arrivalCity город назначения
     * @return список рейсов
     */
    List<FlightDto> findFlightsByCities(String departureCity, String arrivalCity);

    /**
     * Найти рейсы по времени отправления и прибытия
     * @param departureTime время отправления
     * @param arrivalTime время прибытия
     * @return список рейсов
     */
    List<FlightDto> findFlightsByTimes(String departureTime, String arrivalTime);

    /**
     * Обновить доступные места на рейсе
     * @param flightNumber номер рейса
     * @param seatsToBook количество мест для бронирования
     * @return обновленный рейс
     */
    FlightDto updateAvailableSeats(String flightNumber, int seatsToBook);



}
