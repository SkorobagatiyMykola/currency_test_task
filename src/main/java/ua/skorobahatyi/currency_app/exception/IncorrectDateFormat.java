package ua.skorobahatyi.currency_app.exception;

import org.springframework.http.HttpStatus;

public class IncorrectDateFormat extends GenericSystemException {
    public IncorrectDateFormat(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
