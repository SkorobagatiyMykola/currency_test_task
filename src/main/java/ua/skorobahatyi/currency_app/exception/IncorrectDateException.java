package ua.skorobahatyi.currency_app.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class IncorrectDateException extends GenericSystemException {
    public IncorrectDateException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }

    public static IncorrectDateException getException(){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String message = String.format("Check your date-param, to get a correct answer," +
                " the date must be between 06-01-1996 and %s", formatter.format(LocalDate.now()));

        return new IncorrectDateException(status,message);
    }
}