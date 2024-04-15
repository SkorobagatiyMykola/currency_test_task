package ua.skorobahatyi.currency_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

public class CurrencyRatesNotFoundException extends GenericSystemException {

    public CurrencyRatesNotFoundException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }


}
