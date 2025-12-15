package rut.miit.airportweb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rut.miit.airportweb.dto.UserRegistrationDto;
import rut.miit.airportweb.service.UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    private final UserService userService;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("pageTitle", "Аэропорт - Главная");
        return "public/home";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }
        model.addAttribute("pageTitle", "Вход в систему");
        return "public/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        log.info("Обращение к registerPage");
        model.addAttribute("registerRequest", new UserRegistrationDto());
        model.addAttribute("pageTitle", "Регистрация");
        return "public/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerRequest") UserRegistrationDto userRegistrationDto,
                               RedirectAttributes redirectAttributes) {
        log.info("=== Регистрация: {}", userRegistrationDto);
        try {
            userService.registerUser(userRegistrationDto);
            redirectAttributes.addFlashAttribute("successMessage", "Регистрация успешна!");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Ошибка регистрации", e);
            return "redirect:/register?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("pageTitle", "Панель управления");

        // Просто возвращаем шаблон dashboard, который сам определит роль
        return "public/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Доступ запрещен");
        return "error/403";
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("pageTitle", "О системе");
        return "public/about";
    }

    @GetMapping("/contact")
    public String contactPage(Model model) {
        model.addAttribute("pageTitle", "Контакты");
        return "public/contact";
    }
}