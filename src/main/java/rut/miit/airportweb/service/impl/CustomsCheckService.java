package rut.miit.airportweb.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rut.miit.airportweb.dto.CustomsCheckResultDto;
import rut.miit.airportweb.dto.PassengerDto;
import rut.miit.airportweb.dto.TicketDto;
import rut.miit.airportweb.service.PassengerService;
import rut.miit.airportweb.service.TicketService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomsCheckService {

    private final PassengerService passengerService;
    private final TicketService ticketService;

    /**
     * Полная проверка пассажира для таможенного контроля
     */
    public CustomsCheckResultDto performFullCheck(String passportNumber, String ticketNumber) {
        try {
            // 1. Проверяем паспорт
            PassengerService.PassportVerificationResult passportResult =
                    passengerService.verifyPassport(passportNumber);

            if (!passportResult.isValid()) {
                return CustomsCheckResultDto.builder()
                        .passportVerified(false)
                        .passportMessage(passportResult.getMessage())
                        .allChecksPassed(false)
                        .verificationMessage("❌ Паспорт не прошел проверку: " + passportResult.getMessage())
                        .build();
            }

            PassengerDto passenger = passportResult.getPassenger();

            // 2. Проверяем багаж
            boolean luggageVerified = passenger.getLuggageChecked() != null &&
                    passenger.getLuggageChecked();

            // 3. Проверяем билет (если указан)
            boolean ticketVerified = false;
            TicketDto ticket = null;

            if (ticketNumber != null && !ticketNumber.isEmpty()) {
                try {
                    ticket = ticketService.getTicketByNumber(ticketNumber);

                    // Проверяем, что билет принадлежит пассажиру
                    List<TicketDto> passengerTickets = ticketService.findAllByPassportNumber(passportNumber);
                    ticketVerified = passengerTickets.stream()
                            .anyMatch(t -> t.getTicketNumber().equals(ticketNumber));

                    if (!ticketVerified) {
                        return CustomsCheckResultDto.builder()
                                .passenger(passenger)
                                .ticket(ticket)
                                .passportVerified(true)
                                .luggageVerified(luggageVerified)
                                .ticketVerified(false)
                                .allChecksPassed(false)
                                .verificationMessage("❌ Билет не принадлежит данному пассажиру")
                                .build();
                    }
                } catch (Exception e) {
                    return CustomsCheckResultDto.builder()
                            .passenger(passenger)
                            .passportVerified(true)
                            .luggageVerified(luggageVerified)
                            .ticketVerified(false)
                            .allChecksPassed(false)
                            .verificationMessage("❌ Билет не найден: " + e.getMessage())
                            .build();
                }
            } else {
                // Если билет не указан, просто проверяем что у пассажира есть активные билеты
                List<TicketDto> passengerTickets = ticketService.findAllByPassportNumber(passportNumber);
                ticketVerified = !passengerTickets.isEmpty();
                if (!passengerTickets.isEmpty()) {
                    ticket = passengerTickets.get(0);
                }
            }

            // 4. Все проверки пройдены?
            boolean allChecksPassed = passportResult.isValid() && luggageVerified && ticketVerified;

            String message;
            if (allChecksPassed) {
                message = "✅ Все проверки пройдены успешно. Пассажир может пройти таможенный контроль.";
            } else {
                message = "⚠ Требуется внимание: ";
                if (!passportResult.isValid()) message += "Паспорт не проверен. ";
                if (!luggageVerified) message += "Багаж не сдан. ";
                if (!ticketVerified) message += "Проблема с билетом. ";
            }

            return CustomsCheckResultDto.builder()
                    .passenger(passenger)
                    .ticket(ticket)
                    .passportVerified(passportResult.isValid())
                    .passportMessage(passportResult.getMessage())
                    .luggageVerified(luggageVerified)
                    .ticketVerified(ticketVerified)
                    .allChecksPassed(allChecksPassed)
                    .verificationMessage(message)
                    .build();

        } catch (Exception e) {
            return CustomsCheckResultDto.builder()
                    .allChecksPassed(false)
                    .verificationMessage("❌ Ошибка проверки: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Пометить пассажира как прошедшего таможенный контроль
     */
    public void markAsCustomsCleared(String passportNumber) {
        // Здесь можно добавить логику для сохранения записи о проверке
        // Например, создать запись в таблице customs_checks или обновить статус билета

        // Пока просто логируем
        System.out.println("Пассажир " + passportNumber + " прошел таможенный контроль");
    }
}