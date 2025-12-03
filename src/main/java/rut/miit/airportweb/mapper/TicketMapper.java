package rut.miit.airportweb.mapper;

import lombok.experimental.UtilityClass;
import rut.miit.airportweb.dao.entity.TicketEntity;
import rut.miit.airportweb.dto.TicketDto;

@UtilityClass
public class TicketMapper {

    public static TicketDto map(TicketEntity ticket) {
        return TicketDto.builder()
                .id(ticket.getId())
                .flightId(ticket.getFlight().getId())
                .passengerId(ticket.getPassenger().getId())
                .seatNumber(ticket.getSeatNumber())
                .price(ticket.getPrice())
                .ticketNumber(ticket.getTicketNumber())
                .status(ticket.getStatus().toString())
                .bookingDate(ticket.getBookingDate())
                .flightNumber(ticket.getFlight().getFlightNumber())
                .passengerName(getFullName(ticket))
                .passportNumber(ticket.getPassenger().getPassportNumber())
                .build();
    }

    private static String getFullName(TicketEntity ticket) {
        return ticket.getPassenger().getUser().getFirstName() + " " + ticket.getPassenger().getUser().getLastName();
    }
}
