package rut.miit.airportweb.mapper;

import lombok.experimental.UtilityClass;
import rut.miit.airportweb.dao.entity.PassengerEntity;
import rut.miit.airportweb.dao.entity.TicketEntity;
import rut.miit.airportweb.dto.PassengerDto;
import rut.miit.airportweb.dto.TicketDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class PassengerMapper {

    public static PassengerDto map(PassengerEntity passenger) {
        return PassengerDto.builder()
                .id(passenger.getId())
                .user(UserMapper.map(passenger.getUser()))
                .passportNumber(passenger.getPassportNumber())
                .phone(passenger.getPhone())
                .email(passenger.getEmail())
                .luggageChecked(passenger.getLuggageChecked())
                .tickets(mapTicketsToDto(passenger.getTickets()))
                .build();
    }

    private static List<TicketDto> mapTicketsToDto(List<TicketEntity> ticketEntities) {
        if (ticketEntities == null || ticketEntities.isEmpty()) {
            return new ArrayList<>();
        }

        return ticketEntities.stream()
                .map(TicketMapper::map)
                .collect(Collectors.toList());
    }

}
