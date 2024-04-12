package ua.skorobahatyi.currency_app.exception;

import org.springframework.http.HttpStatus;

public class IncorrectDateFormate extends GenericSystemException {
    public IncorrectDateFormate(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
