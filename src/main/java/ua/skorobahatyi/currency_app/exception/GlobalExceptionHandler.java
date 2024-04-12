package ua.skorobahatyi.currency_app.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({GenericSystemException.class})
    public ResponseEntity<ErrorResponse> appException(GenericSystemException e) {

        HttpStatus httpStatus = e.getHttpStatus();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(httpStatus.value());
        errorResponse.setMessage(e.getMessage());

        logger.error("Exception {}", errorResponse.getMessage());

        return new ResponseEntity<>(errorResponse, httpStatus);
    }
}