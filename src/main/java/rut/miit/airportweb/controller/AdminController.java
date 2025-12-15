package rut.miit.airportweb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rut.miit.airportweb.dto.FlightCreateDto;
import rut.miit.airportweb.dto.FlightDto;
import rut.miit.airportweb.dto.UserDto;
import rut.miit.airportweb.service.FlightService;
import rut.miit.airportweb.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final FlightService flightService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Панель администратора");
        model.addAttribute("username", AuthenticationHelper.getCurrentUsername());

        // Статистика для дашборда
        List<FlightDto> flights = flightService.getFlightsList();
        List<UserDto> users = userService.getAllUsersOptimized();

        model.addAttribute("totalFlights", flights.size());
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("activeFlights", flights.stream()
                .filter(f -> f.getStatus().equals("SCHEDULED") || f.getStatus().equals("BOARDING"))
                .count());

        return "admin/dashboard";
    }

    // ========== УПРАВЛЕНИЕ РЕЙСАМИ ==========

    @GetMapping("/flights")
    public String listFlights(Model model) {
        List<FlightDto> flights = flightService.getFlightsList();
        model.addAttribute("flights", flights);
        model.addAttribute("pageTitle", "Управление рейсами");
        return "admin/flights/list";
    }

    @GetMapping("/flights/create")
    public String showCreateFlightForm(Model model) {
        model.addAttribute("flightCreateDto", new FlightCreateDto());
        model.addAttribute("pageTitle", "Создание рейса");
        return "admin/flights/create";
    }

    @PostMapping("/flights/create")
    public String createFlight(@Valid @ModelAttribute FlightCreateDto flightCreateDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Создание рейса - ошибка");
            return "admin/flights/create";
        }

        try {
            String currentUsername = AuthenticationHelper.getCurrentUsername();
            flightService.createFlight(flightCreateDto, currentUsername);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Рейс успешно создан: " + flightCreateDto.getFlightNumber());
            return "redirect:/admin/flights";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка создания рейса: " + e.getMessage());
            model.addAttribute("pageTitle", "Создание рейса - ошибка");
            return "admin/flights/create";
        }
    }

    @GetMapping("/flights/{flightNumber}")
    public String viewFlight(@PathVariable String flightNumber, Model model) {
        try {
            FlightDto flight = flightService.getFlightByNumber(flightNumber);
            model.addAttribute("flight", flight);
            model.addAttribute("pageTitle", "Рейс " + flightNumber);
            return "admin/flights/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Рейс не найден: " + e.getMessage());
            return "redirect:/admin/flights";
        }
    }

    @PostMapping("/flights/{flightNumber}/delete")
    public String deleteFlight(@PathVariable String flightNumber,
                               RedirectAttributes redirectAttributes) {
        try {
            flightService.deleteFlight(flightNumber);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Рейс успешно удален: " + flightNumber);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка удаления рейса: " + e.getMessage());
        }
        return "redirect:/admin/flights";
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
        return "admin/flights/search";
    }

    // ========== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ==========

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<UserDto> users = userService.getAllUsersOptimized();
        model.addAttribute("users", users);
        model.addAttribute("pageTitle", "Управление пользователями");
        return "admin/users/list";
    }

    @GetMapping("/users/{username}")
    public String viewUser(@PathVariable String username, Model model) {
        try {
            UserDto user = userService.getUserByUsername(username);
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Пользователь " + username);
            return "admin/users/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Пользователь не найден: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{username}/delete")
    public String deleteUser(@PathVariable String username,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь успешно удален: " + username);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка удаления пользователя: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/passengers")
    public String listPassengers(Model model) {
        List<UserDto> passengers = userService.getAllPassengers();
        model.addAttribute("passengers", passengers);
        model.addAttribute("pageTitle", "Управление пассажирами");
        return "admin/users/passengers";
    }

    // ========== СТАТИСТИКА И ОТЧЕТЫ ==========

    @GetMapping("/reports")
    public String reports(Model model) {
        List<FlightDto> flights = flightService.getFlightsList();

        // Простая статистика
        long scheduled = flights.stream().filter(f -> f.getStatus().equals("SCHEDULED")).count();
        long boarding = flights.stream().filter(f -> f.getStatus().equals("BOARDING")).count();
        long departed = flights.stream().filter(f -> f.getStatus().equals("DEPARTED")).count();
        long arrived = flights.stream().filter(f -> f.getStatus().equals("ARRIVED")).count();

        model.addAttribute("totalFlights", flights.size());
        model.addAttribute("scheduledFlights", scheduled);
        model.addAttribute("boardingFlights", boarding);
        model.addAttribute("departedFlights", departed);
        model.addAttribute("arrivedFlights", arrived);
        model.addAttribute("pageTitle", "Отчеты и статистика");

        return "admin/reports";
    }
}