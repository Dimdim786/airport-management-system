package rut.miit.airportweb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.airportweb.dao.entity.FlightEntity;
import rut.miit.airportweb.dao.entity.PassengerEntity;
import rut.miit.airportweb.dao.entity.TicketEntity;
import rut.miit.airportweb.dao.repository.FlightRepository;
import rut.miit.airportweb.dao.repository.PassengerRepository;
import rut.miit.airportweb.dao.repository.TicketRepository;
import rut.miit.airportweb.dto.TicketCreateDto;
import rut.miit.airportweb.dto.TicketDto;
import rut.miit.airportweb.dto.TicketUpdateDto;
import rut.miit.airportweb.exception.EntityNotFoundException;
import rut.miit.airportweb.exception.NotPermittedOperation;
import rut.miit.airportweb.mapper.TicketMapper;
import rut.miit.airportweb.service.TicketService;
// Добавляем импорт для исключения
import rut.miit.airportweb.exception.EntityAlreadyExistsException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicketByNumber(String ticketNumber) {
        return this.ticketRepository.findByTicketNumber(ticketNumber)
                .map(TicketMapper::map)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Ticket with ticket number %s not found", ticketNumber)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TicketDto createTicket(TicketCreateDto ticketCreateDto) {
        // Проверяем существование рейса
        FlightEntity flight = flightRepository.findByFlightNumber(ticketCreateDto.getFlightNumberOfTicket())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Flight with number %s not found",
                                ticketCreateDto.getFlightNumberOfTicket())));

        // Проверяем существование пассажира
        PassengerEntity passenger = passengerRepository.findByPassportNumber(ticketCreateDto.getPassportNumberOfPassenger())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Passenger with passport %s not found",
                                ticketCreateDto.getPassportNumberOfPassenger())));

        // Проверяем доступность места
        if (!isSeatAvailable(flight.getFlightNumber(), ticketCreateDto.getSeatNumber())) {
            throw new NotPermittedOperation(
                    String.format("Seat %s is already taken on flight %s",
                            ticketCreateDto.getSeatNumber(), flight.getFlightNumber()));
        }

        // Проверяем, есть ли свободные места
        if (flight.getAvailableSeats() <= 0) {
            throw new NotPermittedOperation("No available seats on this flight");
        }

        // Проверяем, не существует ли уже билет с таким номером
        if (ticketRepository.findByTicketNumber(ticketCreateDto.getTicketNumber()).isPresent()) {
            throw new EntityAlreadyExistsException(
                    String.format("Ticket with number %s already exists",
                            ticketCreateDto.getTicketNumber()));
        }

        // Создаем билет
        TicketEntity ticket = TicketMapper.map(ticketCreateDto, flight, passenger);

        // Сохраняем билет
        TicketEntity savedTicket = ticketRepository.save(ticket);

        // Обновляем количество доступных мест
        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        log.info("Created ticket {} for passenger {} on flight {}",
                ticketCreateDto.getTicketNumber(),
                passenger.getPassportNumber(),
                flight.getFlightNumber());

        return TicketMapper.map(savedTicket);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TicketDto updateTicket(String ticketNumber, TicketUpdateDto ticketUpdateDto) {
        TicketEntity ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Ticket with number %s not found", ticketNumber)));

        // Если меняем место, проверяем доступность
        if (ticketUpdateDto.getSeatNumber() != null &&
                !ticketUpdateDto.getSeatNumber().equals(ticket.getSeatNumber())) {

            if (!isSeatAvailable(ticket.getFlight().getFlightNumber(), ticketUpdateDto.getSeatNumber())) {
                throw new NotPermittedOperation(
                        String.format("Seat %s is already taken", ticketUpdateDto.getSeatNumber()));
            }
        }

        // Обновляем сущность
        TicketMapper.updateEntity(ticket, ticketUpdateDto);

        TicketEntity updatedTicket = ticketRepository.save(ticket);
        log.info("Updated ticket {}", ticketNumber);

        return TicketMapper.map(updatedTicket);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteTicket(String ticketNumber) {
        TicketEntity ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Ticket with number %s not found", ticketNumber)));

        // Освобождаем место
        FlightEntity flight = ticket.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
        flightRepository.save(flight);

        // Удаляем билет
        ticketRepository.delete(ticket);

        log.info("Deleted ticket {} and freed seat on flight {}",
                ticketNumber, flight.getFlightNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> findAllByFlight(String flightNumber) {
        return this.ticketRepository.findAllByFlight(flightNumber)
                .stream()
                .map(TicketMapper::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> findAllByPassportNumber(String passportNumber) {
        return this.ticketRepository.findAllByPassportNumber(passportNumber)
                .stream()
                .map(TicketMapper::map)
                .toList();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TicketDto updateTicketStatus(String ticketNumber, String newStatus) {
        TicketEntity ticket = this.ticketRepository.updateTicketStatusAndGet(ticketNumber, newStatus)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Ticket with number %s not found", ticketNumber)));

        log.info("Updated ticket {} status to {}", ticketNumber, newStatus);
        return TicketMapper.map(ticket);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TicketDto checkInPassenger(String ticketNumber) {
        TicketEntity ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Ticket with number %s not found", ticketNumber)));

        // Проверяем, можно ли зарегистрироваться (рейс должен быть SCHEDULED или BOARDING)
        FlightEntity.FlightStatus flightStatus = ticket.getFlight().getStatus();
        if (flightStatus != FlightEntity.FlightStatus.SCHEDULED &&
                flightStatus != FlightEntity.FlightStatus.BOARDING) {
            throw new NotPermittedOperation(
                    String.format("Cannot check-in for flight with status %s", flightStatus));
        }

        // Обновляем статус билета
        ticket.setStatus(TicketEntity.TicketStatus.CHECKED_IN);
        TicketEntity updatedTicket = ticketRepository.save(ticket);

        log.info("Passenger checked in for ticket {}", ticketNumber);
        return TicketMapper.map(updatedTicket);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TicketDto boardPassenger(String ticketNumber) {
        TicketEntity ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Ticket with number %s not found", ticketNumber)));

        // Проверяем, что пассажир зарегистрирован
        if (ticket.getStatus() != TicketEntity.TicketStatus.CHECKED_IN) {
            throw new NotPermittedOperation("Passenger must be checked in before boarding");
        }

        // Проверяем, что рейс в статусе BOARDING
        if (ticket.getFlight().getStatus() != FlightEntity.FlightStatus.BOARDING) {
            throw new NotPermittedOperation("Flight is not in boarding status");
        }

        // Обновляем статус билета
        ticket.setStatus(TicketEntity.TicketStatus.BOARDED);
        TicketEntity updatedTicket = ticketRepository.save(ticket);

        log.info("Passenger boarded for ticket {}", ticketNumber);
        return TicketMapper.map(updatedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getOccupiedSeats(String flightNumber) {
        return findAllByFlight(flightNumber)
                .stream()
                .map(TicketDto::getSeatNumber)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSeatAvailable(String flightNumber, String seatNumber) {
        List<String> occupiedSeats = getOccupiedSeats(flightNumber);
        return !occupiedSeats.contains(seatNumber);
    }

    @Override
    public long countCheckedInByFlight(String flightNumber) {
        return ticketRepository.countByFlightFlightNumberAndStatus(
                flightNumber, "CHECKED_IN");
    }

}