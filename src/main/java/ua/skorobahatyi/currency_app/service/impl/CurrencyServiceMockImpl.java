package ua.skorobahatyi.currency_app.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ua.skorobahatyi.currency_app.entity.Currency;
import ua.skorobahatyi.currency_app.entity.dto.CurrencyDto;
import ua.skorobahatyi.currency_app.entity.dto.CurrencyResponseDto;
import ua.skorobahatyi.currency_app.entity.dto.DeleteMessage;
import ua.skorobahatyi.currency_app.exception.DateFormatException;
import ua.skorobahatyi.currency_app.exception.GenericSystemException;
import ua.skorobahatyi.currency_app.exception.IncorrectDateException;
import ua.skorobahatyi.currency_app.repository.CurrencyRepository;
import ua.skorobahatyi.currency_app.service.CurrencyService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Profile("mock")
public class CurrencyServiceMockImpl implements CurrencyService {
    private static final Logger logger = LogManager.getLogger(CurrencyServiceMockImpl.class);

    private final CurrencyRepository currencyRepository;

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static ChronoLocalDate oldDate = LocalDate.of(1996, 01, 06);

    @Override
    public CurrencyResponseDto findCurrencyRatesByDate(String dateStr) throws GenericSystemException {
        logger.info("============ :" + dateStr);

        LocalDate date = checkAndGetCorrectDate(dateStr);
        var currencies = currencyRepository.findAll();
        //var currencies = currencyRepository.findAllByDateId(date);
        logger.info("===== Get data from DataBase =====");
        var response = convertCorrectResponse(date, currencies);

        return response;
    }

    @Override
    public DeleteMessage deleteCurrencyRatesByDate(String dateStr) throws GenericSystemException {
        return null;
    }

    @PostConstruct
    private void postConstruct() {
        logger.info("I am mock");

        logger.info("============================");
    }

    private CurrencyResponseDto convertCorrectResponse(LocalDate date, List<Currency> currencies) {
        ZoneId timeZoneId = ZoneId.of("Europe/Kiev");
        var list = currencies.stream()
                .map(el -> new CurrencyDto(el.getCode(),
                        el.getName(),
                        OffsetDateTime.ofInstant(el.getCreatedAt(), timeZoneId),
                        el.getRate().stripTrailingZeros()))
                .toList();
        var response = new CurrencyResponseDto(date, list);

        return response;
    }

    private static LocalDate checkAndGetCorrectDate(String dateStr) throws GenericSystemException {
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, FORMATTER);
        } catch (DateTimeParseException ex) {
            throw DateFormatException.getException();
        }

        if (date.isBefore(oldDate) || date.isAfter(LocalDate.now())) {
            throw IncorrectDateException.getException();
        }

        return date;
    }
}
