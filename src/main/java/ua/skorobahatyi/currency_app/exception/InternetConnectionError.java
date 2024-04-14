package ua.skorobahatyi.currency_app.exception;

import org.springframework.http.HttpStatus;

public class InternetConnectionError extends GenericSystemException {
    public InternetConnectionError(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }

    public static InternetConnectionError getException(String url) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = String.format("Check your Internet connection, the link %s is not available", url);

        return new InternetConnectionError(status, message);
    }
}