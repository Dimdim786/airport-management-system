package rut.miit.airportweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BorderCheckResultDto {
    private PassengerDto passenger;
    private TicketDto ticket;
    private boolean passportValid;
    private boolean passportExpired;
    private boolean visaRequired;
    private boolean visaValid;
    private boolean ticketValid;
    private boolean borderClearance;
    private String verificationMessage;
    private String recommendations;

    public BorderCheckResultDto() {
        this.passportValid = false;
        this.ticketValid = false;
        this.borderClearance = false;
    }
}