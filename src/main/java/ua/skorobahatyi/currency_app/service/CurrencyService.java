package ua.skorobahatyi.currency_app.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ua.skorobahatyi.currency_app.entity.Currency;
import ua.skorobahatyi.currency_app.entity.dto.CurrencyDto;
import ua.skorobahatyi.currency_app.entity.dto.CurrencyResponseDto;
import ua.skorobahatyi.currency_app.entity.dto.DeleteMessage;
import ua.skorobahatyi.currency_app.exception.GenericSystemException;
import ua.skorobahatyi.currency_app.exception.IncorrectDateRatesException;
import ua.skorobahatyi.currency_app.exception.InternetConnectionError;
import ua.skorobahatyi.currency_app.repository.CurrencyRepository;

import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private static final Logger logger = LogManager.getLogger(CurrencyService.class);
    @Value("${bank.gov.ua.currency.url}")
    private String nbuCurrencyUrl;
    private final RestTemplate restTemplate;
    private final CurrencyRepository currencyRepository;
    private static Set<String> dates = new ConcurrentSkipListSet<>();

    private static ChronoLocalDate oldDate = LocalDate.of(1996, 01, 06);

    public CurrencyResponseDto findCurrencyRatesByDate(LocalDate date) throws GenericSystemException {

        if (date.isBefore(oldDate) || date.isAfter(LocalDate.now())) {
            String message = String.format("Check your date-params, to get a correct answer, the date must be between %s and %s", oldDate, LocalDate.now());
            throw new IncorrectDateRatesException(HttpStatus.BAD_REQUEST, message);
        }

        String key = date.toString();
        List<Currency> currencies = null;
        if (dates.contains(key)) {
            currencies = currencyRepository.findAllByDateId(date);
            logger.info("===== Get data from DataBase =====");
        } else {
            currencies = getCurrencyRatesFromNBU(date);
            currencyRepository.saveAll(currencies);
            dates.add(key);
        }

        var response = convertCorrectResponse(date, currencies);

        return response;
    }

    private List<Currency> getCurrencyRatesFromNBU(LocalDate date) throws GenericSystemException {
        logger.info("===== Get data from NBU, begin =====");

        String url = nbuCurrencyUrl + "&date=" + date.toString().replaceAll("-", "");
        logger.info("Request url: {}", url);

        JsonNode jsonResponse;
        try {
            jsonResponse = restTemplate.getForObject(url, JsonNode.class);
        } catch (RestClientException ex) {
            String message = String.format("Check your Internet connection, the link %s is not available", url);
            throw new InternetConnectionError(HttpStatus.INTERNAL_SERVER_ERROR, message);
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

    @PostConstruct
    private void postConstruct() {
        logger.info("===== Initialization for set dates =====");
        List<String> list = currencyRepository.findDistinctDates();
        dates.addAll(list);
        logger.info("==== DataBase has {} dates ====", list.size());
    }


    public DeleteMessage deleteCurrencyRatesByDate(LocalDate dateId) {
        var currencies = currencyRepository.findAllByDateId(dateId);

        if (currencies.isEmpty()) {
            return new DeleteMessage(dateId, "There are no records in the database");
        } else {
            int size = currencies.size();
            currencyRepository.deleteAll(currencies);
            dates.remove(dateId.toString());
            return new DeleteMessage(dateId, size + " records have been deleted from the DB");
        }
    }
}
