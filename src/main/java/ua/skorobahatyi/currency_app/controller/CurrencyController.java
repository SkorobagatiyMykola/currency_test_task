package ua.skorobahatyi.currency_app.controller;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.skorobahatyi.currency_app.entity.dto.CurrencyResponseDto;
import ua.skorobahatyi.currency_app.entity.dto.DeleteMessage;
import ua.skorobahatyi.currency_app.exception.GenericSystemException;
import ua.skorobahatyi.currency_app.exception.IncorrectDateFormate;
import ua.skorobahatyi.currency_app.service.CurrencyService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/currencies")
public class CurrencyController {
    private static final Logger logger = LogManager.getLogger(CurrencyController.class);

    private final CurrencyService currencyService;
    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @GetMapping("")
    public ResponseEntity<CurrencyResponseDto> getActualRateCurrency() throws GenericSystemException {
        LocalDate dateNow = LocalDate.now();
        logger.debug("=== GET currency rates on the actual date: {}", dateNow);
        CurrencyResponseDto response = currencyService.findCurrencyRatesByDate(dateNow);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{dateId}")
    public ResponseEntity<CurrencyResponseDto> getRateCurrencyToDate(@PathVariable("dateId") String dateId) throws GenericSystemException {
        LocalDate dateReport = checkAndGetCorrectDate(dateId);
        logger.debug("=== GET currency rates on the date: {}", dateReport);

        CurrencyResponseDto response = currencyService.findCurrencyRatesByDate(dateReport);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{dateId}")
    public ResponseEntity<DeleteMessage> deleteRateCurrencyByDate(@PathVariable("dateId") String dateId) throws GenericSystemException{
        LocalDate dateReport = checkAndGetCorrectDate(dateId);
        logger.debug("=== GET currency rates on the date: {}", dateReport);
        DeleteMessage response = currencyService.deleteCurrencyRatesByDate(dateReport);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private static LocalDate checkAndGetCorrectDate(String dateId) throws IncorrectDateFormate {
        try {
            return LocalDate.parse(dateId,FORMATTER);
        } catch (DateTimeParseException ex) {
            String message = "Incorrect date format, to get a correct answer, the date must be in this format: dd-mm-yyyy";
            throw new IncorrectDateFormate(HttpStatus.BAD_REQUEST, message);
        }
    }
}
