package ua.skorobahatyi.currency_app.exception;

import org.springframework.http.HttpStatus;

public class DateFormatException extends GenericSystemException {

    public DateFormatException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
    public static DateFormatException getException(){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Incorrect date format, to get a correct answer, the date must be in this format: dd-mm-yyyy";
        return new DateFormatException(status,message);
    }
}
