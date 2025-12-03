package rut.miit.airportweb.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;
import rut.miit.airportweb.dao.entity.FlightEntity;
import rut.miit.airportweb.dto.FlightDto;

@UtilityClass
public class FlightMapper {

    public static FlightDto map(FlightEntity flight) {
        return FlightDto.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .departureCity(flight.getDepartureCity())
                .arrivalCity(flight.getArrivalCity())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .createdBy(UserMapper.map(flight.getCreatedBy()))
                .build();
    }

}
