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
import ua.skorobahatyi.currency_app.service.CurrencyService;
import ua.skorobahatyi.currency_app.service.impl.CurrencyServiceProdImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/currencies")
public class CurrencyController {
    private static final Logger logger = LogManager.getLogger(CurrencyController.class);
    private final CurrencyService currencyService;

    @GetMapping("")
    public ResponseEntity<CurrencyResponseDto> getActualCurrencyRates() throws GenericSystemException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateNow = formatter.format(LocalDate.now());
        logger.debug("=== GET currency rates on the actual date: {}", dateNow);
        CurrencyResponseDto response = currencyService.findCurrencyRatesByDate(dateNow);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{dateId}")
    public ResponseEntity<CurrencyResponseDto> getCurrencyRatesOnDate(@PathVariable("dateId") String dateStr) throws GenericSystemException {
      //  LocalDate dateReport = checkAndGetCorrectDate(dateId);
        logger.debug("=== GET currency rates on the date: {}", dateStr);

        CurrencyResponseDto response = currencyService.findCurrencyRatesByDate(dateStr);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{dateId}")
    public ResponseEntity<DeleteMessage> deleteRateCurrencyOnDate(@PathVariable("dateId") String dateStr) throws GenericSystemException {
        logger.debug("=== GET currency rates on the date: {}", dateStr);
        DeleteMessage response = currencyService.deleteCurrencyRatesByDate(dateStr);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
