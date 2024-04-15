package ua.skorobahatyi.currency_app.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ua.skorobahatyi.currency_app.entity.Currency;
import ua.skorobahatyi.currency_app.entity.dto.CurrencyDto;
import ua.skorobahatyi.currency_app.entity.dto.CurrencyResponseDto;
import ua.skorobahatyi.currency_app.entity.dto.DeleteMessage;
import ua.skorobahatyi.currency_app.exception.*;
import ua.skorobahatyi.currency_app.repository.CurrencyRepository;
import ua.skorobahatyi.currency_app.service.CurrencyService;

import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Profile("!mock")
public class CurrencyServiceProdImpl implements CurrencyService {
    private static final Logger logger = LogManager.getLogger(CurrencyServiceProdImpl.class);
    @Value("${bank.gov.ua.currency.url}")
    private String nbuCurrencyUrl;
    private final RestTemplate restTemplate;
    private final CurrencyRepository currencyRepository;
    private static Set<String> dates = new ConcurrentSkipListSet<>();
    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static ChronoLocalDate oldDate = LocalDate.of(1996, 01, 06);

    public CurrencyResponseDto findCurrencyRatesByDate(String dateStr) throws GenericSystemException {
        LocalDate date = checkAndGetCorrectDate(dateStr);

        List<Currency> currencies = null;
        if (dates.contains(dateStr)) {
            currencies = currencyRepository.findAllByDateId(date);
            logger.info("===== Get data from DataBase =====");
        } else {
            currencies = getCurrencyRatesFromNBU(date);
            currencyRepository.saveAll(currencies);
            dates.add(dateStr);
        }

        var response = convertCorrectResponse(date, currencies);

        return response;
    }

    public DeleteMessage deleteCurrencyRatesByDate(String dateStr) throws GenericSystemException {
        LocalDate date = checkAndGetCorrectDate(dateStr);

        var currencies = currencyRepository.findAllByDateId(date);

        if (currencies.isEmpty()) {
            return new DeleteMessage(date, "There are no records in the database");
        } else {
            int size = currencies.size();
            currencyRepository.deleteAll(currencies);
            dates.remove(dateStr);
            return new DeleteMessage(date, size + " records have been deleted from the DB");
        }
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

    private List<Currency> getCurrencyRatesFromNBU(LocalDate date) throws GenericSystemException {
        logger.info("===== Get data from NBU, begin =====");

        String url = nbuCurrencyUrl + "&date=" + date.toString().replaceAll("-", "");
        logger.info("Request url: {}", url);

        JsonNode jsonResponse;
        try {
            jsonResponse = restTemplate.getForObject(url, JsonNode.class);
        } catch (RestClientException ex) {
            throw InternetConnectionError.getException(url);
        }

        var currencies = StreamSupport.stream(jsonResponse.spliterator(), true)
                .map(el -> new Currency(date,
                        el.get("r030").asInt(),
                        el.get("cc").asText(),
                        el.get("txt").asText(),
                        el.get("rate").decimalValue(),
                        Instant.now().truncatedTo(ChronoUnit.SECONDS)
                ))
                .toList();
        logger.info("===== Get data from NBU, end (mapping dates completed) =====");

        return currencies;
    }

    @PostConstruct
    private void postConstruct() {
        logger.info("===== Initialization for set dates =====");
        List<String> list = currencyRepository.findDistinctDates();
        dates.addAll(list);
        logger.info("==== DataBase has {} dates ====", list.size());
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
}
