package rut.miit.airportweb.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rut.miit.airportweb.dto.*;
import rut.miit.airportweb.exception.EntityAlreadyExistsException;
import rut.miit.airportweb.exception.EntityNotFoundException;
import rut.miit.airportweb.exception.NotPermittedOperation;
import rut.miit.airportweb.service.FlightService;
import rut.miit.airportweb.service.PassengerService;
import rut.miit.airportweb.service.TicketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/passenger")
@PreAuthorize("hasRole('ROLE_PASSENGER')")
@RequiredArgsConstructor
@Slf4j
public class PassengerController {

    private final FlightService flightService;
    private final TicketService ticketService;
    private final PassengerService passengerService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String username = AuthenticationHelper.getCurrentUsername();
        model.addAttribute("username", username);
        model.addAttribute("pageTitle", "Панель пассажира");

        try {
            // Получаем информацию о пассажире
            var passengers = passengerService.findByUsername(username);
            if (!passengers.isEmpty()) {
                var passenger = passengers.getFirst();

                if (passenger == null) {
                    log.info("Passenger is null");
                }

                model.addAttribute("passenger", passenger);

                //REMOVE AFTER DEBUG
                if (passenger.getPassportNumber() == null) {
                    log.warn("Passport is null");
                }


                // Получаем билеты пассажира
                List<TicketDto> tickets = ticketService.findAllByPassportNumber(passenger.getPassportNumber());
                model.addAttribute("tickets", tickets);
                model.addAttribute("activeTickets", tickets.stream()
                        .filter(t -> t.getStatus().equals("BOOKED") || t.getStatus().equals("CHECKED_IN"))
                        .count());
            }
        } catch (Exception e) {
            log.warn("Could not load passenger data: {}", e.getMessage());
        }

        return "passenger/dashboard";
    }

    // ========== ПРОСМОТР РЕЙСОВ ==========

    @GetMapping("/flights")
    public String viewFlights(Model model) {
        List<FlightDto> flights = flightService.getFlightsList();
        model.addAttribute("flights", flights);
        model.addAttribute("pageTitle", "Доступные рейсы");
        return "passenger/flights/list";
    }

//    @GetMapping("/flights/available")
//    public String viewAvailableFlights(Model model) {
//        List<FlightDto> flights = flightService.findAvailableFlights();
//        model.addAttribute("flights", flights);
//        model.addAttribute("pageTitle", "Рейсы со свободными местами");
//        return "passenger/flights/available";
//    }

    @GetMapping("/flights/{flightNumber}")
    public String viewFlightDetails(@PathVariable String flightNumber, Model model) {
        try {
            FlightDto flight = flightService.getFlightByNumber(flightNumber);
            model.addAttribute("flight", flight);
            model.addAttribute("pageTitle", "Рейс " + flightNumber);

            // Получаем занятые места
            List<String> occupiedSeats = ticketService.getOccupiedSeats(flightNumber);
            model.addAttribute("occupiedSeats", occupiedSeats);
            model.addAttribute("availableSeats", flight.getAvailableSeats());

            return "passenger/flights/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
            return "redirect:/passenger/flights";
        }
    }

    @GetMapping("/flights/search")
    public String searchFlights(@RequestParam(required = false) String departureCity,
                                @RequestParam(required = false) String arrivalCity,
                                Model model) {
        if (departureCity != null && arrivalCity != null) {
            List<FlightDto> flights = flightService.findFlightsByCities(departureCity, arrivalCity);
            model.addAttribute("flights", flights);
            model.addAttribute("departureCity", departureCity);
            model.addAttribute("arrivalCity", arrivalCity);
        }
        model.addAttribute("pageTitle", "Поиск рейсов");
        return "passenger/flights/search";
    }

    // ========== УПРАВЛЕНИЕ БИЛЕТАМИ ==========

    @GetMapping("/tickets")
    public String myTickets(Model model) {
        String username = AuthenticationHelper.getCurrentUsername();

        try {
            var passengers = passengerService.findByUsername(username);
            if (!passengers.isEmpty()) {
                var passenger = passengers.get(0);
                List<TicketDto> tickets = ticketService.findAllByPassportNumber(passenger.getPassportNumber());
                model.addAttribute("tickets", tickets);
            }
        } catch (Exception e) {
            log.warn("Could not load tickets: {}", e.getMessage());
        }

        model.addAttribute("pageTitle", "Мои билеты");
        return "passenger/tickets/list";
    }

    @GetMapping("/tickets/{ticketNumber}")
    public String viewTicket(@PathVariable String ticketNumber, Model model) {
        try {
            TicketDto ticket = ticketService.getTicketByNumber(ticketNumber);
            model.addAttribute("ticket", ticket);
            model.addAttribute("pageTitle", "Билет " + ticketNumber);

            // Проверяем, можно ли зарегистрироваться
            boolean canCheckIn = ticket.getStatus().equals("BOOKED");
            model.addAttribute("canCheckIn", canCheckIn);

            // Проверяем, можно ли пройти на посадку
            boolean canBoard = ticket.getStatus().equals("CHECKED_IN");
            model.addAttribute("canBoard", canBoard);

            return "passenger/tickets/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Билет не найден: " + e.getMessage());
            return "redirect:/passenger/tickets";
        }
    }

    @PostMapping("/tickets/{ticketNumber}/checkin")
    public String checkIn(@PathVariable String ticketNumber,
                          RedirectAttributes redirectAttributes) {
        try {
            ticketService.checkInPassenger(ticketNumber);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрация на рейс успешно выполнена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка регистрации: " + e.getMessage());
        }
        return "redirect:/passenger/tickets/" + ticketNumber;
    }

    @PostMapping("/tickets/{ticketNumber}/cancel")
    public String cancelTicket(@PathVariable String ticketNumber,
                               RedirectAttributes redirectAttributes) {
        try {
            // Получаем билет перед удалением (для информации)
            TicketDto ticket = ticketService.getTicketByNumber(ticketNumber);

            // Удаляем билет (сервис сам освободит место)
            ticketService.deleteTicket(ticketNumber);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Бронирование билета " + ticketNumber + " успешно отменено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при отмене бронирования: " + e.getMessage());
        }
        return "redirect:/passenger/tickets";
    }

    // ========== ПРОФИЛЬ ПАССАЖИРА ==========

    @GetMapping("/profile")
    public String profile(Model model) {
        String username = AuthenticationHelper.getCurrentUsername();

        try {
            var passengers = passengerService.findByUsername(username);
            if (!passengers.isEmpty()) {
                model.addAttribute("passenger", passengers.get(0));
            }
        } catch (Exception e) {
            log.warn("Could not load profile: {}", e.getMessage());
        }

        model.addAttribute("pageTitle", "Мой профиль");
        return "passenger/profile";
    }

    @PostMapping("/profile/luggage")
    public String updateLuggageStatus(@RequestParam String passportNumber,
                                      @RequestParam boolean luggageChecked,
                                      RedirectAttributes redirectAttributes) {
        try {
            passengerService.updateLuggageStatus(passportNumber, luggageChecked);
            String message = luggageChecked ?
                    "Багаж отмечен как сдан" : "Багаж отмечен как не сдан";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка обновления статуса багажа: " + e.getMessage());
        }
        return "redirect:/passenger/profile";
    }

// ========== БРОНИРОВАНИЕ БИЛЕТОВ ==========

//    @GetMapping("/tickets/book")
//    public String showBookingForm(@RequestParam String flightNumber, Model model) {
//        try {
//            // Получаем информацию о рейсе
//            FlightDto flight = flightService.getFlightByNumber(flightNumber);
//
//            // Получаем информацию о текущем пассажире
//            String username = AuthenticationHelper.getCurrentUsername();
//            var passengers = passengerService.findByUsername(username);
//            PassengerDto passenger = passengers.isEmpty() ? null : passengers.get(0);
//
//            // Получаем занятые места
//            List<String> occupiedSeats = ticketService.getOccupiedSeats(flightNumber);
//
//            model.addAttribute("flight", flight);
//            model.addAttribute("passenger", passenger);
//            model.addAttribute("occupiedSeats", occupiedSeats);
//            model.addAttribute("ticketBookingDto", new TicketBookingDto());
//            model.addAttribute("pageTitle", "Бронирование рейса " + flightNumber);
//
//            return "passenger/tickets/book";
//
//        } catch (Exception e) {
//            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
//            return "redirect:/passenger/flights";
//        }
//    }

    // GET метод для отображения формы
    @GetMapping("/tickets/book")
    public String showBookingForm(@RequestParam String flightNumber,
                                  Model model,
                                  @ModelAttribute("successMessage") String successMessage,
                                  @ModelAttribute("errorMessage") String errorMessage) {
        try {
            FlightDto flight = flightService.getFlightByNumber(flightNumber);
            String username = AuthenticationHelper.getCurrentUsername();
            var passengers = passengerService.findByUsername(username);
            PassengerDto passenger = passengers.isEmpty() ? null : passengers.get(0);
            List<String> occupiedSeats = ticketService.getOccupiedSeats(flightNumber);

            model.addAttribute("flight", flight);
            model.addAttribute("passenger", passenger);
            model.addAttribute("occupiedSeats", occupiedSeats);
            model.addAttribute("pageTitle", "Бронирование рейса " + flightNumber);

            // Добавляем сообщения из flash атрибутов
            if (successMessage != null && !successMessage.isEmpty()) {
                model.addAttribute("successMessage", successMessage);
            }
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.addAttribute("errorMessage", errorMessage);
            }

            return "passenger/tickets/book";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
            return "redirect:/passenger/flights";
        }
    }

    // POST метод для бронирования
    @PostMapping("/tickets/book")
    public String bookTicket(@RequestParam String flightNumber,
                             @RequestParam String seatNumber,
                             @RequestParam String passportNumber,
                             @RequestParam String passengerName,
                             @RequestParam(defaultValue = "5000") BigDecimal price,
                             RedirectAttributes redirectAttributes) {

        try {
            // Генерируем номер билета
            String ticketNumber = generateTicketNumber();

            // Создаем DTO для создания билета
            TicketCreateDto ticketCreateDto = TicketCreateDto.builder()
                    .flightNumberOfTicket(flightNumber)
                    .passportNumberOfPassenger(passportNumber)
                    .seatNumber(seatNumber)
                    .price(price)
                    .ticketNumber(ticketNumber)
                    .build();

            // Создаем билет через сервис
            TicketDto createdTicket = ticketService.createTicket(ticketCreateDto);

            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Билет успешно забронирован! Номер вашего билета: " + createdTicket.getTicketNumber());
            return "redirect:/passenger/dashboard";

        } catch (EntityAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Это место уже занято. Пожалуйста, выберите другое место.");
            return "redirect:/passenger/tickets/book?flightNumber=" + flightNumber;
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Ошибка: " + e.getMessage());
            return "redirect:/passenger/tickets/book?flightNumber=" + flightNumber;
        } catch (NotPermittedOperation e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Невозможно забронировать билет: " + e.getMessage());
            return "redirect:/passenger/tickets/book?flightNumber=" + flightNumber;
        } catch (Exception e) {
            log.error("Error booking ticket", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Произошла ошибка при бронировании: " + e.getMessage());
            return "redirect:/passenger/tickets/book?flightNumber=" + flightNumber;
        }
    }

    // Метод для генерации номера билета
    private String generateTicketNumber() {
        // Формат: TKT-ГГММДД-XXXXX
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String datePart = now.format(formatter);
        String randomPart = String.format("%05d", (int)(Math.random() * 100000));
        return "TKT-" + datePart + "-" + randomPart;
    }
}