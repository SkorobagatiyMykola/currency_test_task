package ua.skorobahatyi.currency_app.exception;

import org.springframework.http.HttpStatus;

public class IncorrectDateRatesException extends GenericSystemException {
    public IncorrectDateRatesException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
