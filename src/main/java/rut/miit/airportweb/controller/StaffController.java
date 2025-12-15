package rut.miit.airportweb.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rut.miit.airportweb.dto.BoardingPassDto;
import rut.miit.airportweb.dto.FlightDto;
import rut.miit.airportweb.dto.PassengerDto;
import rut.miit.airportweb.dto.TicketDto;
import rut.miit.airportweb.exception.EntityNotFoundException;
import rut.miit.airportweb.service.BoardingPassService;
import rut.miit.airportweb.service.FlightService;
import rut.miit.airportweb.service.PassengerService;
import rut.miit.airportweb.service.TicketService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasRole('ROLE_AIRPORT_STAFF')")
@RequiredArgsConstructor
@Slf4j
public class StaffController {

    private final FlightService flightService;
    private final TicketService ticketService;
    private final BoardingPassService boardingPassService;
    private final PassengerService passengerService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("username", AuthenticationHelper.getCurrentUsername());
        model.addAttribute("pageTitle", "Панель сотрудника аэропорта");

        // Получаем рейсы для посадки
        List<FlightDto> boardingFlights = flightService.findFlightsByStatus("BOARDING");
        model.addAttribute("boardingFlights", boardingFlights);

        // Получаем рейсы по расписанию
        List<FlightDto> scheduledFlights = flightService.findFlightsByStatus("SCHEDULED");
        model.addAttribute("scheduledFlights", scheduledFlights);

        return "staff/dashboard";
    }

    // ========== УПРАВЛЕНИЕ ПОСАДКОЙ ==========

    @GetMapping("/boarding")
    public String boardingManagement(Model model) {
        List<FlightDto> boardingFlights = flightService.findFlightsByStatus("BOARDING");
        model.addAttribute("boardingFlights", boardingFlights);
        model.addAttribute("pageTitle", "Управление посадкой");
        return "staff/boarding/list";
    }

    @GetMapping("/boarding/flight/{flightNumber}")
    public String flightBoarding(@PathVariable String flightNumber, Model model) {
        try {
            FlightDto flight = flightService.getFlightByNumber(flightNumber);
            List<TicketDto> tickets = ticketService.findAllByFlight(flightNumber);

            model.addAttribute("flight", flight);
            model.addAttribute("tickets", tickets);
            model.addAttribute("pageTitle", "Посадка на рейс " + flightNumber);

            // Статистика по посадке
            long totalPassengers = tickets.size();
            long checkedIn = tickets.stream().filter(t -> t.getStatus().equals("CHECKED_IN")).count();
            long boarded = tickets.stream().filter(t -> t.getStatus().equals("BOARDED")).count();

            model.addAttribute("totalPassengers", totalPassengers);
            model.addAttribute("checkedInPassengers", checkedIn);
            model.addAttribute("boardedPassengers", boarded);

            return "staff/boarding/flight";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
            return "redirect:/staff/boarding";
        }
    }

    @GetMapping("/boarding/ticket/{ticketNumber}")
    public String passengerBoarding(@PathVariable String ticketNumber, Model model) {
        try {
            TicketDto ticket = ticketService.getTicketByNumber(ticketNumber);
            BoardingPassDto boardingPass = boardingPassService.getBoardingPassByTicketNumber(ticketNumber);

            model.addAttribute("ticket", ticket);
            model.addAttribute("boardingPass", boardingPass);
            model.addAttribute("pageTitle", "Посадка пассажира");

            // Проверяем готовность к посадке
            if (boardingPass != null) {
                var readiness = boardingPassService.checkBoardingReadiness(boardingPass.getId());
                model.addAttribute("readinessCheck", readiness);
            }

            return "staff/boarding/passenger";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Билет не найден: " + e.getMessage());
            return "redirect:/staff/boarding";
        }
    }

    @PostMapping("/boarding/ticket/{ticketNumber}/board")
    public String boardPassenger(@PathVariable String ticketNumber,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Обновляем статус билета
            ticketService.boardPassenger(ticketNumber);

            // Обновляем статус в посадочном талоне
            BoardingPassDto boardingPass = boardingPassService.getBoardingPassByTicketNumber(ticketNumber);
            if (boardingPass != null) {
                boardingPassService.updateBoardingStatus(boardingPass.getId(), true);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Пассажир успешно посажен в самолет");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка посадки: " + e.getMessage());
        }
        return "redirect:/staff/boarding/ticket/" + ticketNumber;
    }

    @PostMapping("/boarding/ticket/{ticketNumber}/unboard")
    public String unboardPassenger(@PathVariable String ticketNumber,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Обновляем статус в посадочном талоне
            BoardingPassDto boardingPass = boardingPassService.getBoardingPassByTicketNumber(ticketNumber);
            if (boardingPass != null) {
                boardingPassService.updateBoardingStatus(boardingPass.getId(), false);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Статус посадки сброшен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: " + e.getMessage());
        }
        return "redirect:/staff/boarding/ticket/" + ticketNumber;
    }

    // ========== РЕГИСТРАЦИЯ ПАССАЖИРОВ ==========

    @GetMapping("/checkin")
    public String checkInManagement(Model model) {
        List<FlightDto> scheduledFlights = flightService.findFlightsByStatus("SCHEDULED");
        model.addAttribute("scheduledFlights", scheduledFlights);
        model.addAttribute("pageTitle", "Регистрация пассажиров");
        return "staff/checkin/list";
    }

    // В классе StaffController, метод flightCheckIn

    @GetMapping("/checkin/flight/{flightNumber}")
    public String flightCheckIn(@PathVariable String flightNumber, Model model) {
        try {
            FlightDto flight = flightService.getFlightByNumber(flightNumber);
            List<TicketDto> allTickets = ticketService.findAllByFlight(flightNumber);

            // Разделяем билеты по статусам
            List<TicketDto> registeredTickets = allTickets.stream()
                    .filter(t -> "CHECKED_IN".equals(t.getStatus()))
                    .collect(Collectors.toList());

            List<TicketDto> notRegisteredTickets = allTickets.stream()
                    .filter(t -> "BOOKED".equals(t.getStatus()))
                    .collect(Collectors.toList());

            model.addAttribute("flight", flight);
            model.addAttribute("allTickets", allTickets);
            model.addAttribute("registeredTickets", registeredTickets);
            model.addAttribute("notRegisteredTickets", notRegisteredTickets);
            model.addAttribute("pageTitle", "Регистрация на рейс " + flightNumber);

            // Статистика по регистрации
            long totalPassengers = allTickets.size();
            long booked = notRegisteredTickets.size();
            long checkedIn = registeredTickets.size();

            model.addAttribute("totalPassengers", totalPassengers);
            model.addAttribute("bookedPassengers", booked);
            model.addAttribute("checkedInPassengers", checkedIn);

            return "staff/checkin/flight";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
            return "redirect:/staff/checkin";
        }
    }

    @PostMapping("/checkin/ticket/{ticketNumber}/register")
    public String registerPassenger(@PathVariable String ticketNumber,
                                    RedirectAttributes redirectAttributes) {
        try {
            ticketService.checkInPassenger(ticketNumber);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пассажир успешно зарегистрирован");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка регистрации: " + e.getMessage());
        }
        return "redirect:/staff/checkin";
    }

    // ========== УПРАВЛЕНИЕ БАГАЖОМ ==========

//    @GetMapping("/luggage")
//    public String luggageManagement(Model model) {
//        List<FlightDto> todayFlights = flightService.getFlightsList().stream()
//                .filter(f -> f.getStatus().equals("SCHEDULED") || f.getStatus().equals("BOARDING"))
//                .limit(10) // Ограничиваем для производительности
//                .toList();
//
//        model.addAttribute("flights", todayFlights);
//        model.addAttribute("pageTitle", "Управление багажом");
//        return "staff/luggage/list";
//    }

//    @GetMapping("/luggage/flight/{flightNumber}")
//    public String flightLuggage(@PathVariable String flightNumber, Model model) {
//        try {
//            FlightDto flight = flightService.getFlightByNumber(flightNumber);
//            List<TicketDto> tickets = ticketService.findAllByFlight(flightNumber);
//
//            model.addAttribute("flight", flight);
//            model.addAttribute("tickets", tickets);
//            model.addAttribute("pageTitle", "Багаж рейса " + flightNumber);
//
//            return "staff/luggage/flight";
//        } catch (Exception e) {
//            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
//            return "redirect:/staff/luggage";
//        }
//    }

    // ========== СТАТУСЫ РЕЙСОВ ==========

//    @GetMapping("/flights")
//    public String flightStatus(Model model) {
//        List<FlightDto> flights = flightService.getFlightsList();
//        model.addAttribute("flights", flights);
//        model.addAttribute("pageTitle", "Статусы рейсов");
//        return "staff/flights/list";
//    }

    @PostMapping("/flights/{flightNumber}/status")
    public String updateFlightStatus(@PathVariable String flightNumber,
                                     @RequestParam String newStatus,
                                     RedirectAttributes redirectAttributes) {
        try {
            // В реальном приложении здесь была бы отдельная служба для обновления статуса рейса
            // Пока просто показываем сообщение
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Статус рейса %s изменен на: %s", flightNumber, newStatus));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка обновления статуса: " + e.getMessage());
        }
        return "redirect:/staff/flights";
    }

    // ========== ПРОВЕРКА ПАССАЖИРОВ ==========

    @GetMapping("/passengers/check")
    public String checkPassenger(@RequestParam(required = false) String passportNumber,
                                 @RequestParam(required = false) String ticketNumber,
                                 Model model) {

        model.addAttribute("pageTitle", "Проверка пассажира");

        try {
            if (passportNumber != null && !passportNumber.isEmpty()) {
                // Поиск по паспорту
                PassengerDto passenger = passengerService.getPassengerByPassportNumber(passportNumber);
                List<TicketDto> tickets = ticketService.findAllByPassportNumber(passportNumber);

                model.addAttribute("passenger", passenger);
                model.addAttribute("tickets", tickets);
                model.addAttribute("foundBy", "паспорт");
                model.addAttribute("searchValue", passportNumber);

            } else if (ticketNumber != null && !ticketNumber.isEmpty()) {
                // Поиск по билету
                TicketDto ticket = ticketService.getTicketByNumber(ticketNumber);
                PassengerDto passenger = passengerService.getPassengerByPassportNumber(ticket.getPassportNumber());
                List<TicketDto> tickets = ticketService.findAllByPassportNumber(ticket.getPassportNumber());

                model.addAttribute("passenger", passenger);
                model.addAttribute("tickets", tickets);
                model.addAttribute("currentTicket", ticket);
                model.addAttribute("foundBy", "билет");
                model.addAttribute("searchValue", ticketNumber);

            } else {
                // Просто форма поиска
                model.addAttribute("message", "Введите данные для поиска");
            }

        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Пассажир не найден: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка поиска: " + e.getMessage());
        }

        return "staff/passengers/check";
    }

    @PostMapping("/passengers/{passportNumber}/verify")
    public String verifyPassenger(@PathVariable String passportNumber,
                                  @RequestParam String verificationType,
                                  @RequestParam boolean verified,
                                  RedirectAttributes redirectAttributes) {

        try {
            // Валидация паспорта
            PassengerService.PassportVerificationResult result =
                    passengerService.verifyPassport(passportNumber);

            if (result.isValid()) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Паспорт проверен и валиден. Пассажир: " +
                                result.getPassenger().getUser().getFirstName() + " " +
                                result.getPassenger().getUser().getLastName());
            } else {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Проблема с паспортом: " + result.getMessage());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка проверки паспорта: " + e.getMessage());
        }

        return "redirect:/staff/passengers/check?passportNumber=" + passportNumber;
    }

    @PostMapping("/passengers/{passportNumber}/luggage")
    public String updateLuggageStatus(@PathVariable String passportNumber,
                                      @RequestParam boolean luggageChecked,
                                      RedirectAttributes redirectAttributes) {

        try {
            PassengerDto updatedPassenger = passengerService.updateLuggageStatus(passportNumber, luggageChecked);

            String message = luggageChecked ?
                    "Багаж отмечен как сдан" : "Багаж отмечен как не сдан";

            redirectAttributes.addFlashAttribute("successMessage", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка обновления статуса багажа: " + e.getMessage());
        }

        return "redirect:/staff/passengers/check?passportNumber=" + passportNumber;
    }

    // Обновим метод flightStatus для поддержки фильтрации
    @GetMapping("/flights")
    public String flightStatus(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String departureCity,
                               @RequestParam(required = false) String arrivalCity,
                               Model model) {

        List<FlightDto> flights = flightService.getFlightsList();

        // Применяем фильтры
        if (status != null && !status.isEmpty()) {
            flights = flights.stream()
                    .filter(f -> f.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        if (departureCity != null && !departureCity.isEmpty()) {
            flights = flights.stream()
                    .filter(f -> f.getDepartureCity().toLowerCase().contains(departureCity.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (arrivalCity != null && !arrivalCity.isEmpty()) {
            flights = flights.stream()
                    .filter(f -> f.getArrivalCity().toLowerCase().contains(arrivalCity.toLowerCase()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("flights", flights);
        model.addAttribute("pageTitle", "Каталог рейсов");

        return "staff/flights/list";
    }

    // ========== УПРАВЛЕНИЕ БАГАЖОМ ==========

    @GetMapping("/luggage")
    public String luggageManagement(Model model) {
        List<FlightDto> todayFlights = flightService.getFlightsList().stream()
                .filter(f -> f.getStatus().equals("SCHEDULED") || f.getStatus().equals("BOARDING"))
                .limit(10)
                .collect(Collectors.toList());

        model.addAttribute("flights", todayFlights);
        model.addAttribute("pageTitle", "Управление багажом");
        return "staff/luggage/list";
    }

    @GetMapping("/luggage/flight/{flightNumber}")
    public String flightLuggage(@PathVariable String flightNumber, Model model) {
        try {
            FlightDto flight = flightService.getFlightByNumber(flightNumber);
            List<TicketDto> tickets = ticketService.findAllByFlight(flightNumber);

            // Получаем информацию о пассажирах
            List<PassengerDto> passengers = new ArrayList<>();
            for (TicketDto ticket : tickets) {
                try {
                    PassengerDto passenger = passengerService.getPassengerByPassportNumber(ticket.getPassportNumber());
                    passengers.add(passenger);
                } catch (EntityNotFoundException e) {
                    log.warn("Passenger not found for passport: {}", ticket.getPassportNumber());
                }
            }

            model.addAttribute("flight", flight);
            model.addAttribute("tickets", tickets);
            model.addAttribute("passengers", passengers);
            model.addAttribute("pageTitle", "Багаж рейса " + flightNumber);

            return "staff/luggage/flight";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
            return "redirect:/staff/luggage";
        }
    }

    @PostMapping("/luggage/passenger/{passportNumber}")
    public String updateLuggageStatus(@PathVariable String passportNumber,
                                      @RequestParam boolean luggageChecked,
                                      @RequestParam(required = false) String flightNumber,
                                      RedirectAttributes redirectAttributes) {

        try {
            PassengerDto updatedPassenger = passengerService.updateLuggageStatus(passportNumber, luggageChecked);

            String message = luggageChecked ?
                    "Багаж пассажира отмечен как сдан" :
                    "Багаж пассажира отмечен как не сдан";

            redirectAttributes.addFlashAttribute("successMessage", message);

            if (flightNumber != null && !flightNumber.isEmpty()) {
                return "redirect:/staff/luggage/flight/" + flightNumber;
            } else {
                return "redirect:/staff/luggage";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка обновления статуса багажа: " + e.getMessage());
            return "redirect:/staff/luggage";
        }
    }

    @GetMapping("/luggage/scan")
    public String scanLuggage(Model model) {
        model.addAttribute("pageTitle", "Сканирование багажа");
        return "staff/luggage/scan";
    }

    @PostMapping("/luggage/scan")
    public String processLuggageScan(@RequestParam String barcode,
                                     RedirectAttributes redirectAttributes) {

        try {
            // Эмуляция сканирования багажа
            // В реальном приложении здесь была бы логика поиска по штрих-коду

            redirectAttributes.addFlashAttribute("successMessage",
                    "Багаж отсканирован. Штрих-код: " + barcode);
            return "redirect:/staff/luggage";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка сканирования: " + e.getMessage());
            return "redirect:/staff/luggage/scan";
        }
    }

    @PostMapping("/luggage/flight/{flightNumber}/bulk")
    public String bulkLuggageUpdate(@PathVariable String flightNumber,
                                    @RequestParam String action,
                                    RedirectAttributes redirectAttributes) {

        try {
            List<TicketDto> tickets = ticketService.findAllByFlight(flightNumber);
            int updatedCount = 0;

            for (TicketDto ticket : tickets) {
                try {
                    if (action.equals("check_all")) {
                        passengerService.updateLuggageStatus(ticket.getPassportNumber(), true);
                        updatedCount++;
                    } else if (action.equals("uncheck_all")) {
                        passengerService.updateLuggageStatus(ticket.getPassportNumber(), false);
                        updatedCount++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to update luggage for passenger: {}", ticket.getPassportNumber());
                }
            }

            String message = action.equals("check_all") ?
                    "Багаж " + updatedCount + " пассажиров отмечен как сданный" :
                    "Статус багажа " + updatedCount + " пассажиров сброшен";

            redirectAttributes.addFlashAttribute("successMessage", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка массового обновления: " + e.getMessage());
        }

        return "redirect:/staff/luggage/flight/" + flightNumber;
    }

    // В StaffController добавляем новый метод

    @PostMapping("/checkin/flight/{flightNumber}/quick")
    public String quickCheckIn(@PathVariable String flightNumber,
                               @RequestParam String passportNumber,
                               RedirectAttributes redirectAttributes) {
        try {
            // Находим все билеты пассажира на этот рейс
            List<TicketDto> passengerTickets = ticketService.findAllByPassportNumber(passportNumber)
                    .stream()
                    .filter(t -> flightNumber.equals(t.getFlightNumber()))
                    .toList();

            if (passengerTickets.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Билет пассажира с паспортом " + passportNumber + " не найден на рейс " + flightNumber);
                return "redirect:/staff/checkin/flight/" + flightNumber;
            }

            // Регистрируем все найденные билеты
            for (TicketDto ticket : passengerTickets) {
                if ("BOOKED".equals(ticket.getStatus())) {
                    ticketService.checkInPassenger(ticket.getTicketNumber());
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Пассажир с паспортом " + passportNumber + " успешно зарегистрирован");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка регистрации: " + e.getMessage());
        }

        return "redirect:/staff/checkin/flight/" + flightNumber;
    }

    // Добавим метод массовой регистрации
    @PostMapping("/checkin/flight/{flightNumber}/bulk")
    public String bulkCheckIn(@PathVariable String flightNumber,
                              RedirectAttributes redirectAttributes) {
        try {
            List<TicketDto> tickets = ticketService.findAllByFlight(flightNumber);
            int registeredCount = 0;

            for (TicketDto ticket : tickets) {
                if ("BOOKED".equals(ticket.getStatus())) {
                    ticketService.checkInPassenger(ticket.getTicketNumber());
                    registeredCount++;
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Зарегистрировано " + registeredCount + " пассажиров");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка массовой регистрации: " + e.getMessage());
        }

        return "redirect:/staff/checkin/flight/" + flightNumber;
    }

}