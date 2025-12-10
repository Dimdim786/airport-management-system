package rut.miit.airportweb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.airportweb.dao.entity.FlightEntity;
import rut.miit.airportweb.dao.entity.UserEntity;
import rut.miit.airportweb.dao.repository.FlightRepository;
import rut.miit.airportweb.dao.repository.UserRepository;
import rut.miit.airportweb.dto.FlightCreateDto;
import rut.miit.airportweb.dto.FlightDto;
import rut.miit.airportweb.exception.EntityNotFoundException;
import rut.miit.airportweb.exception.NotPermittedOperation;
import rut.miit.airportweb.mapper.FlightMapper;
import rut.miit.airportweb.service.FlightService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final UserRepository userRepository;

    @Override
    public FlightDto getFlightByNumber(String flightNumber) {
        FlightEntity flight = this.flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Flight with flight number %s not found", flightNumber)));

        return FlightMapper.map(flight);
    }

    @Override
    public FlightDto createFlight(FlightCreateDto flightCreateDto, String createdByUserUsername) {
        UserEntity creator = this.userRepository.findByUsername(createdByUserUsername)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Creator with username %s not found", createdByUserUsername)));

        FlightEntity flight = FlightMapper.map(flightCreateDto);
        flight.setCreatedBy(creator);
        flight.setTickets(new ArrayList<>());

        FlightEntity savedEntity = this.flightRepository.save(flight);
        log.info("Saving flight with flight number {}", flightCreateDto.getFlightNumber());

        return FlightMapper.map(savedEntity);
    }

    @Override
    public void deleteFlight(String flightNumber) {
        this.flightRepository.findByFlightNumber(flightNumber)
                .ifPresentOrElse(
                        this.flightRepository::delete,
                        () -> {
                            throw new EntityNotFoundException(String.format("Flight with flight number %s not found", flightNumber));
                        }
                );
    }

    @Override
    public List<FlightDto> findFlightsByCities(String departureCity, String arrivalCity) {
        return this.flightRepository.findByDepartureCityAndArrivalCity(departureCity, arrivalCity)
                .stream()
                .map(FlightMapper::map)
                .toList();
    }

    @Override
    public List<FlightDto> findFlightsByTimes(String departureTime, String arrivalTime) {
        return this.flightRepository.findByDepartureTimeAndArrivalTime(departureTime, arrivalTime)
                .stream()
                .map(FlightMapper::map)
                .toList();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FlightDto updateAvailableSeats(String flightNumber, int seatsToBook) {
        FlightEntity flight = this.flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Flight with flight number %s not found", flightNumber)));

        int availableSeats = flight.getAvailableSeats();
        availableSeats -= seatsToBook;

        if (availableSeats < 0) {
            throw new NotPermittedOperation("Available seats cannot be less than zero");
        }

        flight.setAvailableSeats(availableSeats);
        return FlightMapper.map(this.flightRepository.save(flight));
    }

    @Override
    public List<FlightDto> getFlightsList() {
        return this.flightRepository.findAll()
                .stream()
                .map(FlightMapper::map)
                .toList();
    }
}
