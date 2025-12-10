package rut.miit.airportweb.exception;

public class NotPermittedOperation extends RuntimeException {
    public NotPermittedOperation(String message) {
        super(message);
    }
}
