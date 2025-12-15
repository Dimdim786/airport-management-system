package rut.miit.airportweb.service;

import rut.miit.airportweb.dto.TicketCreateDto;
import rut.miit.airportweb.dto.TicketDto;
import rut.miit.airportweb.dto.TicketUpdateDto;

import java.util.List;

/**
 * Сервис для управления билетами
 */
public interface TicketService {

    /**
     * Получить билет по номеру билета
     * @param ticketNumber номер билета
     * @return Optional с билетом
     */
    TicketDto getTicketByNumber(String ticketNumber);

    /**
     * Создать новый билет
     * @param ticketCreateDto DTO создания билета
     * @return созданный билет
     */
    TicketDto createTicket(TicketCreateDto ticketCreateDto);

    /**
     * Обновить билет
     * @param ticketNumber номер билета
     * @param ticketUpdateDto DTO обновления билета
     * @return обновленный билет
     */
    TicketDto updateTicket(String ticketNumber, TicketUpdateDto ticketUpdateDto);

    /**
     * Удалить билет
     * @param id идентификатор билета
     */
    void deleteTicket(String ticketNumber);

    /**
     * Найти все билеты по номеру рейса
     * @param flightNumber номер рейса
     * @return список билетов
     */
    List<TicketDto> findAllByFlight(String flightNumber);

    /**
     * Найти все билеты по номеру паспорта
     * @param passportNumber номер паспорта
     * @return список билетов
     */
    List<TicketDto> findAllByPassportNumber(String passportNumber);

    /**
     * Обновить статус билета
     * @param ticketId ID билета
     * @param newStatus новый статус
     * @return обновленный билет
     */
    TicketDto updateTicketStatus(String ticketNumber, String newStatus);

    /**
     * Зарегистрировать пассажира на рейс (чек-ин)
     * @param ticketNumber Номер билета
     * @return билет после регистрации
     */
    TicketDto checkInPassenger(String ticketNumber);

    /**
     * Посадить пассажира на рейс
     * @param ticketNumber номер билета
     * @return билет после посадки
     */
    TicketDto boardPassenger(String ticketNumber);

    /**
     * Получить занятые места на рейсе
     * @param flightId ID рейса
     * @return список занятых мест
     */
    List<String> getOccupiedSeats(String flightNumber);

    /**
     * Проверить доступность места
     * @param flightNumber номер рейса
     * @param seatNumber номер места
     * @return true если место доступно
     */
    boolean isSeatAvailable(String flightNumber, String seatNumber);

    long countCheckedInByFlight(String flightNumber);

}
