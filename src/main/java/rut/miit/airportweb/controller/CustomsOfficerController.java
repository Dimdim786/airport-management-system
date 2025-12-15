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
import rut.miit.airportweb.service.impl.CustomsCheckService;

import java.util.List;

@Controller
@RequestMapping("/customs")
@PreAuthorize("hasRole('ROLE_CUSTOMS_OFFICER')")
@RequiredArgsConstructor
@Slf4j
public class CustomsOfficerController {

    private final FlightService flightService;
    private final PassengerService passengerService;
    private final TicketService ticketService;
    private final CustomsCheckService customsCheckService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("username", AuthenticationHelper.getCurrentUsername());
        model.addAttribute("pageTitle", "Панель таможенного контроля");

        // Получаем рейсы для проверки
        List<FlightDto> boardingFlights = flightService.findFlightsByStatus("BOARDING");
        model.addAttribute("boardingFlights", boardingFlights);

        return "customs/dashboard";
    }

    // ========== ПРОВЕРКА ПАССАЖИРА ==========

    @GetMapping("/check")
    public String checkPassenger(@RequestParam(required = false) String passportNumber,
                                 @RequestParam(required = false) String ticketNumber,
                                 Model model) {

        model.addAttribute("pageTitle", "Проверка пассажира");

        if (passportNumber != null && !passportNumber.isEmpty()) {
            try {
                // Выполняем полную проверку
                CustomsCheckResultDto checkResult = customsCheckService
                        .performFullCheck(passportNumber, ticketNumber);

                model.addAttribute("checkResult", checkResult);
                model.addAttribute("passportNumber", passportNumber);
                model.addAttribute("ticketNumber", ticketNumber);

            } catch (Exception e) {
                model.addAttribute("errorMessage", "Ошибка проверки: " + e.getMessage());
            }
        }

        return "customs/check";
    }

    @PostMapping("/check/approve")
    public String approvePassenger(@RequestParam String passportNumber,
                                   @RequestParam(required = false) String ticketNumber,
                                   RedirectAttributes redirectAttributes) {

        try {
            // Помечаем как прошедшего контроль
            customsCheckService.markAsCustomsCleared(passportNumber);

            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Пассажир " + passportNumber + " успешно прошел таможенный контроль");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Ошибка: " + e.getMessage());
        }

        return "redirect:/customs/check?passportNumber=" + passportNumber +
                (ticketNumber != null ? "&ticketNumber=" + ticketNumber : "");
    }

    @PostMapping("/check/reject")
    public String rejectPassenger(@RequestParam String passportNumber,
                                  @RequestParam(required = false) String ticketNumber,
                                  @RequestParam(required = false) String reason,
                                  RedirectAttributes redirectAttributes) {

        try {
            String officerUsername = AuthenticationHelper.getCurrentUsername();
            log.warn("Таможенник {} отклонил пассажира {}. Причина: {}",
                    officerUsername, passportNumber, reason);

            redirectAttributes.addFlashAttribute("warningMessage",
                    "⚠ Пассажир " + passportNumber + " отправлен на дополнительную проверку. " +
                            "Причина: " + (reason != null ? reason : "не указана"));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Ошибка: " + e.getMessage());
        }

        return "redirect:/customs/check?passportNumber=" + passportNumber +
                (ticketNumber != null ? "&ticketNumber=" + ticketNumber : "");
    }

    // ========== ПОИСК ПАССАЖИРОВ ПО РЕЙСУ ==========

    @GetMapping("/flight/{flightNumber}")
    public String flightPassengers(@PathVariable String flightNumber, Model model) {
        try {
            FlightDto flight = flightService.getFlightByNumber(flightNumber);
            List<TicketDto> tickets = ticketService.findAllByFlight(flightNumber);

            // Получаем информацию о пассажирах
            for (TicketDto ticket : tickets) {
                try {
                    PassengerDto passenger = passengerService.getPassengerByPassportNumber(ticket.getPassportNumber());
                    // Можно добавить passenger в ticket или собрать отдельный список
                } catch (Exception e) {
                    log.warn("Не удалось получить пассажира для билета: {}", ticket.getTicketNumber());
                }
            }

            model.addAttribute("flight", flight);
            model.addAttribute("tickets", tickets);
            model.addAttribute("pageTitle", "Пассажиры рейса " + flightNumber);

            return "customs/flight";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
            return "redirect:/customs/dashboard";
        }
    }

    // ========== БЫСТРЫЙ ПОИСК ==========

    @GetMapping("/search")
    public String quickSearch(@RequestParam(required = false) String query,
                              Model model) {

        model.addAttribute("pageTitle", "Поиск пассажира");

        if (query != null && !query.isEmpty()) {
            // Пробуем поиск по паспорту
            try {
                PassengerDto passenger = passengerService.getPassengerByPassportNumber(query);
                model.addAttribute("passenger", passenger);
                model.addAttribute("foundBy", "паспорт");
            } catch (Exception e1) {
                // Пробуем поиск по билету
                try {
                    TicketDto ticket = ticketService.getTicketByNumber(query);
                    PassengerDto passenger = passengerService.getPassengerByPassportNumber(ticket.getPassportNumber());
                    model.addAttribute("passenger", passenger);
                    model.addAttribute("ticket", ticket);
                    model.addAttribute("foundBy", "билет");
                } catch (Exception e2) {
                    model.addAttribute("errorMessage", "Ничего не найдено по запросу: " + query);
                }
            }
        }

        return "customs/search";
    }
}