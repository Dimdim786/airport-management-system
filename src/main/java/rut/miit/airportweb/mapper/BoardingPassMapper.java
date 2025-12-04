package rut.miit.airportweb.mapper;

import lombok.experimental.UtilityClass;
import rut.miit.airportweb.dao.entity.BoardingPassEntity;
import rut.miit.airportweb.dto.BoardingPassDto;

@UtilityClass
public class BoardingPassMapper {

    public static BoardingPassDto map(BoardingPassEntity boardingPass) {
        return BoardingPassDto.builder()
                .id(boardingPass.getId())
                .ticket(TicketMapper.map(boardingPass.getTicket()))
                .checkInTime(boardingPass.getCheckInTime())
                .passportVerified(boardingPass.getPassportVerified())
                .luggageVerified(boardingPass.getLuggageVerified())
                .boarded(boardingPass.getBoarded())
                .verifiedByBorderGuard(UserMapper.map(boardingPass.getVerifiedByBorderGuard()))
                .verifiedByCustoms(UserMapper.map(boardingPass.getVerifiedByCustoms()))
                .build();

    }

}
