package rut.miit.airportweb.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rut.miit.airportweb.dto.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BorderCheckService {

    private final PassengerService passengerService;
    private final TicketService ticketService;
    private final FlightService flightService;

    // Список стран, требующих визу для граждан РФ (пример)
    private static final List<String> VISA_REQUIRED_COUNTRIES = List.of(
            "USA", "CANADA", "UK", "AUSTRALIA", "JAPAN", "CHINA"
    );

    // Список запрещенных стран (пример)
    private static final List<String> RESTRICTED_COUNTRIES = List.of(
            "NORTH_KOREA", "SYRIA", "IRAN"
    );

    /**
     * Проверка паспорта на действительность
     */
    private boolean checkPassportValidity(String passportNumber) {
        // Здесь должна быть логика проверки действительности паспорта
        // Например, проверка по базе недействительных паспортов

        // Простая проверка формата
        Pattern passportPattern = Pattern.compile("^[A-Z0-9]{6,10}$");
        return passportPattern.matcher(passportNumber).matches();
    }

    /**
     * Проверка визовых требований
     */
    private VisaCheckResult checkVisaRequirements(String departureCity, String arrivalCity,
                                                  String passportNumber) {
        // Упрощенная логика:
        // 1. Проверяем, требуется ли виза
        // 2. Проверяем, действительна ли виза

        boolean visaRequired = VISA_REQUIRED_COUNTRIES.contains(arrivalCity.toUpperCase());
        boolean visaValid = false;
        String message = "";

        if (visaRequired) {
            // Здесь должна быть проверка визы в базе данных
            // Пока делаем заглушку
            visaValid = Math.random() > 0.3; // 70% шанс что виза действительна
            message = visaValid ? "Визовые документы в порядке" : "Проблема с визой";
        } else {
            message = "Виза не требуется";
        }

        return new VisaCheckResult(visaRequired, visaValid, message);
    }

    /**
     * Проверка страны на ограничения
     */
    private boolean isCountryRestricted(String countryCode) {
        return RESTRICTED_COUNTRIES.contains(countryCode.toUpperCase());
    }

    /**
     * Полная пограничная проверка
     */
    public BorderCheckResultDto performBorderCheck(String passportNumber, String ticketNumber) {
        try {
            // 1. Проверяем паспорт через PassengerService
            PassengerService.PassportVerificationResult passportResult =
                    passengerService.verifyPassport(passportNumber);

            if (!passportResult.isValid()) {
                return BorderCheckResultDto.builder()
                        .passportValid(false)
                        .verificationMessage("❌ Паспорт недействителен: " + passportResult.getMessage())
                        .borderClearance(false)
                        .build();
            }

            PassengerDto passenger = passportResult.getPassenger();

            // 2. Получаем билет
            TicketDto ticket = null;
            boolean ticketValid = false;

            if (ticketNumber != null && !ticketNumber.isEmpty()) {
                try {
                    ticket = ticketService.getTicketByNumber(ticketNumber);

                    // Проверяем, что билет принадлежит пассажиру
                    List<TicketDto> passengerTickets = ticketService.findAllByPassportNumber(passportNumber);
                    ticketValid = passengerTickets.stream()
                            .anyMatch(t -> t.getTicketNumber().equals(ticketNumber));

                    if (!ticketValid) {
                        return BorderCheckResultDto.builder()
                                .passenger(passenger)
                                .ticket(ticket)
                                .passportValid(true)
                                .ticketValid(false)
                                .verificationMessage("❌ Билет не принадлежит пассажиру")
                                .borderClearance(false)
                                .build();
                    }

                    // Получаем информацию о рейсе
                    FlightDto flight = flightService.getFlightByNumber(ticket.getFlightNumber());

                    // 3. Проверяем визовые требования
                    VisaCheckResult visaCheck = checkVisaRequirements(
                            flight.getDepartureCity(),
                            flight.getArrivalCity(),
                            passportNumber
                    );

                    // 4. Проверяем ограничения на страну
                    boolean countryRestricted = isCountryRestricted(flight.getArrivalCity());

                    // 5. Формируем рекомендации
                    StringBuilder recommendations = new StringBuilder();
                    if (visaCheck.isVisaRequired() && !visaCheck.isVisaValid()) {
                        recommendations.append("Требуется действительная виза. ");
                    }
                    if (countryRestricted) {
                        recommendations.append("Страна назначения имеет ограничения. ");
                    }

                    // 6. Определяем, можно ли разрешить пересечение границы
                    boolean borderClearance = passportResult.isValid() &&
                            ticketValid &&
                            (!visaCheck.isVisaRequired() || visaCheck.isVisaValid()) &&
                            !countryRestricted;

                    String message;
                    if (borderClearance) {
                        message = "✅ Все проверки пройдены. Разрешено пересечение границы.";
                    } else {
                        message = "⚠ Требуется внимание: ";
                        if (!passportResult.isValid()) message += "Паспорт. ";
                        if (!ticketValid) message += "Билет. ";
                        if (visaCheck.isVisaRequired() && !visaCheck.isVisaValid()) message += "Визовые документы. ";
                        if (countryRestricted) message += "Ограничения страны назначения. ";
                    }

                    return BorderCheckResultDto.builder()
                            .passenger(passenger)
                            .ticket(ticket)
                            .passportValid(passportResult.isValid())
                            .visaRequired(visaCheck.isVisaRequired())
                            .visaValid(visaCheck.isVisaValid())
                            .ticketValid(ticketValid)
                            .borderClearance(borderClearance)
                            .verificationMessage(message)
                            .recommendations(recommendations.toString())
                            .build();

                } catch (Exception e) {
                    return BorderCheckResultDto.builder()
                            .passenger(passenger)
                            .passportValid(true)
                            .ticketValid(false)
                            .verificationMessage("❌ Ошибка проверки билета: " + e.getMessage())
                            .borderClearance(false)
                            .build();
                }
            } else {
                // Если билет не указан, проверяем только паспорт
                boolean borderClearance = passportResult.isValid();

                return BorderCheckResultDto.builder()
                        .passenger(passenger)
                        .passportValid(passportResult.isValid())
                        .ticketValid(false)
                        .borderClearance(borderClearance)
                        .verificationMessage(borderClearance ?
                                "✅ Паспорт действителен" :
                                "❌ Паспорт недействителен")
                        .recommendations("Укажите номер билета для полной проверки")
                        .build();
            }

        } catch (Exception e) {
            return BorderCheckResultDto.builder()
                    .passportValid(false)
                    .verificationMessage("❌ Ошибка проверки: " + e.getMessage())
                    .borderClearance(false)
                    .build();
        }
    }

    /**
     * Пометить пассажира как прошедшего пограничный контроль
     */
    public void markAsBorderCleared(String passportNumber, String officerNotes) {
        // Здесь можно добавить логику для сохранения записи о проверке
        System.out.println("Пассажир " + passportNumber +
                " прошел пограничный контроль. Заметки: " + officerNotes);
    }

    // Вспомогательный класс для результатов проверки визы
    @Getter
    @AllArgsConstructor
    private static class VisaCheckResult {
        private boolean visaRequired;
        private boolean visaValid;
        private String message;
    }
}