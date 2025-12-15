package rut.miit.airportweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CustomsCheckResultDto {
    private PassengerDto passenger;
    private TicketDto ticket;
    private boolean passportVerified;
    private String passportMessage;
    private boolean luggageVerified;
    private boolean ticketVerified;
    private boolean allChecksPassed;
    private String verificationMessage;

    public CustomsCheckResultDto() {
        this.passportVerified = false;
        this.luggageVerified = false;
        this.ticketVerified = false;
        this.allChecksPassed = false;
    }
}