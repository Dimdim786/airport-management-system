package rut.miit.airportweb.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rut.miit.airportweb.dto.*;
import rut.miit.airportweb.service.*;
import rut.miit.airportweb.service.BorderCheckService;

import java.util.List;

@Controller
@RequestMapping("/border")
@PreAuthorize("hasRole('ROLE_BORDER_GUARD')")
@RequiredArgsConstructor
@Slf4j
public class BorderGuardController {

    private final FlightService flightService;
    private final PassengerService passengerService;
    private final TicketService ticketService;
    private final BorderCheckService borderCheckService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("username", AuthenticationHelper.getCurrentUsername());
        model.addAttribute("pageTitle", "Пограничный контроль");

        // Получаем международные рейсы
        List<FlightDto> internationalFlights = flightService.getFlightsList().stream()
                .filter(f -> isInternationalFlight(f))
                .limit(10)
                .toList();

        model.addAttribute("internationalFlights", internationalFlights);

        return "border/dashboard";
    }

    // Простая проверка на международный рейс (по городам)
    private boolean isInternationalFlight(FlightDto flight) {
        // Упрощенная логика: считаем международными все рейсы кроме Москва-СПб
        return !(flight.getDepartureCity().equalsIgnoreCase("Москва") &&
                flight.getArrivalCity().equalsIgnoreCase("Санкт-Петербург"));
    }

    // ========== ПРОВЕРКА ПАССАЖИРА ==========

    @GetMapping("/check")
    public String checkPassenger(@RequestParam(required = false) String passportNumber,
                                 @RequestParam(required = false) String ticketNumber,
                                 Model model) {

        model.addAttribute("pageTitle", "Пограничная проверка");

        // Важно: всегда добавляем номера в модель для отображения в форме
        model.addAttribute("passportNumber", passportNumber != null ? passportNumber : "");
        model.addAttribute("ticketNumber", ticketNumber != null ? ticketNumber : "");

        if (passportNumber != null && !passportNumber.trim().isEmpty()) {
            try {
                // Выполняем пограничную проверку
                BorderCheckResultDto checkResult = borderCheckService
                        .performBorderCheck(passportNumber.trim(),
                                ticketNumber != null ? ticketNumber.trim() : null);

                model.addAttribute("checkResult", checkResult);

            } catch (Exception e) {
                model.addAttribute("errorMessage", "Ошибка проверки: " + e.getMessage());
                log.error("Ошибка пограничной проверки для паспорта: {}", passportNumber, e);
            }
        }

        return "border/check";
    }

    @PostMapping("/check/approve")
    public String approvePassenger(@RequestParam String passportNumber,
                                   @RequestParam(required = false) String ticketNumber,
                                   @RequestParam(required = false) String officerNotes,
                                   RedirectAttributes redirectAttributes) {

        try {
            // Помечаем как прошедшего пограничный контроль
            borderCheckService.markAsBorderCleared(passportNumber, officerNotes);

            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Пассажир " + passportNumber + " успешно прошел пограничный контроль");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Ошибка: " + e.getMessage());
        }

        return "redirect:/border/check?passportNumber=" + passportNumber +
                (ticketNumber != null ? "&ticketNumber=" + ticketNumber : "");
    }

    @PostMapping("/check/reject")
    public String rejectPassenger(@RequestParam String passportNumber,
                                  @RequestParam(required = false) String ticketNumber,
                                  @RequestParam String reason,
                                  @RequestParam(required = false) String action,
                                  RedirectAttributes redirectAttributes) {

        try {
            String officerUsername = AuthenticationHelper.getCurrentUsername();
            log.warn("Пограничник {} отклонил пассажира {}. Причина: {}, Действие: {}",
                    officerUsername, passportNumber, reason, action);

            String message = "⚠ Пассажир " + passportNumber + " не прошел пограничный контроль. ";
            message += "Причина: " + reason;

            if (action != null && !action.isEmpty()) {
                message += ". Действие: " + action;
            }

            redirectAttributes.addFlashAttribute("warningMessage", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Ошибка: " + e.getMessage());
        }

        return "redirect:/border/check?passportNumber=" + passportNumber +
                (ticketNumber != null ? "&ticketNumber=" + ticketNumber : "");
    }

    // ========== МЕЖДУНАРОДНЫЕ РЕЙСЫ ==========

    @GetMapping("/flights/international")
    public String internationalFlights(Model model) {
        List<FlightDto> internationalFlights = flightService.getFlightsList().stream()
                .filter(f -> isInternationalFlight(f))
                .toList();

        model.addAttribute("flights", internationalFlights);
        model.addAttribute("pageTitle", "Международные рейсы");

        return "border/flights/international";
    }

    @GetMapping("/flight/{flightNumber}/passengers")
    public String flightPassengers(@PathVariable String flightNumber, Model model) {
        try {
            FlightDto flight = flightService.getFlightByNumber(flightNumber);
            List<TicketDto> tickets = ticketService.findAllByFlight(flightNumber);

            model.addAttribute("flight", flight);
            model.addAttribute("tickets", tickets);
            model.addAttribute("pageTitle", "Пассажиры рейса " + flightNumber);
            model.addAttribute("isInternational", isInternationalFlight(flight));

            return "border/flight/passengers";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
            return "redirect:/border/dashboard";
        }
    }

    // ========== СПИСКИ И ПРОВЕРКИ ==========

    @GetMapping("/watchlist")
    public String watchlist(Model model) {
        model.addAttribute("pageTitle", "Списки наблюдения");
        // Здесь можно добавить логику для получения списков наблюдения
        return "border/watchlist";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("pageTitle", "Статистика пограничного контроля");
        // Здесь можно добавить статистику
        return "border/statistics";
    }
}