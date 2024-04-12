package ua.skorobahatyi.currency_app.exception;

import org.springframework.http.HttpStatus;

public class InternetConnectionError extends GenericSystemException {
    public InternetConnectionError(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
