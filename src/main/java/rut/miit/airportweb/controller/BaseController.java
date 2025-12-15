package rut.miit.airportweb.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import rut.miit.airportweb.exception.EntityNotFoundException;
import rut.miit.airportweb.exception.EntityAlreadyExistsException;
import rut.miit.airportweb.exception.NotPermittedOperation;

@Slf4j
@ControllerAdvice
public class BaseController {

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException ex, Model model) {
        log.error("Entity not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public String handleEntityAlreadyExists(EntityAlreadyExistsException ex, Model model) {
        log.error("Entity already exists: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/409";
    }

    @ExceptionHandler(NotPermittedOperation.class)
    public String handleNotPermittedOperation(NotPermittedOperation ex, Model model) {
        log.error("Operation not permitted: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Произошла непредвиденная ошибка");
        return "error/500";
    }
}