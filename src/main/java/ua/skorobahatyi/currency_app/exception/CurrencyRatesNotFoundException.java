package ua.skorobahatyi.currency_app.exception;

import org.springframework.http.HttpStatus;

public class CurrencyRatesNotFoundException extends GenericSystemException {

    public CurrencyRatesNotFoundException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }


}
