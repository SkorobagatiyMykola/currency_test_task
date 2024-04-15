package ua.skorobahatyi.currency_app.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ua.skorobahatyi.currency_app.entity.dto.CurrencyDto;
import ua.skorobahatyi.currency_app.entity.dto.CurrencyResponseDto;
import ua.skorobahatyi.currency_app.entity.dto.DeleteMessage;
import ua.skorobahatyi.currency_app.exception.DateFormatException;
import ua.skorobahatyi.currency_app.exception.GenericSystemException;
import ua.skorobahatyi.currency_app.exception.IncorrectDateException;
import ua.skorobahatyi.currency_app.exception.InternetConnectionError;
import ua.skorobahatyi.currency_app.service.impl.CurrencyServiceProdImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyControllerTest {
    @InjectMocks
    CurrencyController currencyController;
    @Mock
    CurrencyServiceProdImpl currencyService;

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static String nbuCurrencyUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    @DisplayName("Get actual currency rates, for successful case")
    @SneakyThrows
    @Test
    void getActualCurrencyRates_shouldReturnResponseEntity_and200StatusCode() {
        LocalDate localDate = LocalDate.now();
        String date = FORMATTER.format(LocalDate.now());
        CurrencyResponseDto responseDto = new CurrencyResponseDto(localDate,
                List.of(new CurrencyDto("USD", "Долар США", OffsetDateTime.now(), new BigDecimal(1.8755)),
                        new CurrencyDto("EUR", "Євро", OffsetDateTime.now(), new BigDecimal(29.131474))));

        when(currencyService.findCurrencyRatesByDate(date)).thenReturn(responseDto);

        ResponseEntity<CurrencyResponseDto> responseEntity = currencyController.getActualCurrencyRates();
        List<CurrencyDto> currencies = responseEntity.getBody().getCurrencyDtoList();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(localDate, responseEntity.getBody().getDate());
        assertEquals(2, currencies.size());
        assertEquals("USD", currencies.get(0).getCode());
        assertEquals("Долар США", currencies.get(0).getName());
        assertEquals(new BigDecimal(1.8755), currencies.get(0).getRate());
        verify(currencyService, times(1)).findCurrencyRatesByDate(date);
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Get actual currency rates (not a DB), when there is no Internet connection")
    @SneakyThrows
    @Test
    void getActualRateCurrency_shouldReturnException_andInternetConnectionError() {
        String date = FORMATTER.format(LocalDate.now());
        String url = nbuCurrencyUrl + "&date=" + date.toString().replaceAll("-", "");
        String message = String.format("Check your Internet connection, the link %s is not available", url);

        when(currencyService.findCurrencyRatesByDate(date))
                .thenThrow(InternetConnectionError.getException(url));

        GenericSystemException ex = assertThrows(GenericSystemException.class, () ->
                currencyController.getActualCurrencyRates());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertEquals(message, ex.getMessage());
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Get currency rates on 12/04/2024, for successful case")
    @SneakyThrows
    @Test
    void getRateCurrencyOnDate_shouldReturnResponseEntity_and200StatusCode() {
        LocalDate localDate = LocalDate.of(2024, 04, 12);
        String date = FORMATTER.format(localDate);
        CurrencyResponseDto responseDto = new CurrencyResponseDto(localDate,
                List.of(new CurrencyDto("USD", "Долар США", OffsetDateTime.now(), new BigDecimal(26.8755)),
                        new CurrencyDto("CAD", "Канадський долар", OffsetDateTime.now(), new BigDecimal(26.7931)),
                        new CurrencyDto("EUR", "Євро", OffsetDateTime.now(), new BigDecimal(29.131474))));

        when(currencyService.findCurrencyRatesByDate(date)).thenReturn(responseDto);

        ResponseEntity<CurrencyResponseDto> responseEntity = currencyController.getCurrencyRatesOnDate(date);
        List<CurrencyDto> currencies = responseEntity.getBody().getCurrencyDtoList();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(localDate, responseEntity.getBody().getDate());
        assertEquals(3, currencies.size());
        assertEquals("USD", currencies.get(0).getCode());
        assertEquals("Долар США", currencies.get(0).getName());
        assertEquals(new BigDecimal(26.8755), currencies.get(0).getRate());
        verify(currencyService, times(1)).findCurrencyRatesByDate(date);
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Get currency rates on 12/04/2024, when data has incorrect format")
    @SneakyThrows
    @Test
    void getRateCurrencyOnDate_shouldReturnException_andIncorrectDateFormat() {
        String dateStr = "2024-04-12";

        when(currencyService.findCurrencyRatesByDate(dateStr))
                .thenThrow(DateFormatException.getException());

        GenericSystemException ex = assertThrows(GenericSystemException.class, () ->
                currencyController.getCurrencyRatesOnDate(dateStr));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals("Incorrect date format, to get a correct answer, the date must be in this format: dd-mm-yyyy", ex.getMessage());
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Get currency rates on 01/01/1996, when data is deprecated (until 06/01/1996)")
    @SneakyThrows
    @Test
    void getRateCurrencyOnDate_shouldReturnException_andIncorrectDateException_VeryOldDate() {
        String dateStr = "01-01-1996";
        String message = String.format("Check your date-param, to get a correct answer, " +
                "the date must be between 06-01-1996 and %s", FORMATTER.format(LocalDate.now()));

        when(currencyService.findCurrencyRatesByDate(dateStr))
                .thenThrow(IncorrectDateException.getException());

        GenericSystemException ex = assertThrows(GenericSystemException.class, () ->
                currencyController.getCurrencyRatesOnDate(dateStr));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals(message, ex.getMessage());
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Get currency rates on future date, when data is a future date (after tomorrow)")
    @SneakyThrows
    @Test
    void getRateCurrencyOnDate_shouldReturnException_andIncorrectDateException_FutureDate() {
        String dateStr = FORMATTER.format(LocalDate.now().plusDays(2));
        String message = String.format("Check your date-param, to get a correct answer, " +
                "the date must be between 06-01-1996 and %s", FORMATTER.format(LocalDate.now()));

        when(currencyService.findCurrencyRatesByDate(dateStr))
                .thenThrow(IncorrectDateException.getException());

        GenericSystemException ex = assertThrows(GenericSystemException.class, () ->
                currencyController.getCurrencyRatesOnDate(dateStr));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals(message, ex.getMessage());
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Get currency rates on 12/04/2024 (not a DB), when there is no Internet connection")
    @SneakyThrows
    @Test
    void getRateCurrencyOnDate_shouldReturnException_andInternetConnectionError() {
        String date = "2024-04-12";
        String url = nbuCurrencyUrl + "&date=" + date.toString().replaceAll("-", "");
        String message = String.format("Check your Internet connection, the link %s is not available", url);

        when(currencyService.findCurrencyRatesByDate(date))
                .thenThrow(InternetConnectionError.getException(url));

        GenericSystemException ex = assertThrows(GenericSystemException.class, () ->
                currencyController.getCurrencyRatesOnDate(date));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertEquals(message, ex.getMessage());
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Delete currency rates on 12/04/2024 (delete 42 records from the DB), for successful case")
    @SneakyThrows
    @Test
    void deleteRateCurrencyOnDate_shouldReturnResponseEntity_and200StatusCode() {
        LocalDate localDate = LocalDate.now().minusDays(2);
        String dateStr = FORMATTER.format(localDate);
        String messageStr = "42 records have been deleted from the DB";
        DeleteMessage message = new DeleteMessage(localDate, messageStr);

        when(currencyService.deleteCurrencyRatesByDate(dateStr)).thenReturn(message);

        ResponseEntity<DeleteMessage> responseEntity = currencyController.deleteRateCurrencyOnDate(dateStr);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(localDate, responseEntity.getBody().getDate());
        assertEquals(messageStr, responseEntity.getBody().getMessage());
        verify(currencyService, times(1)).deleteCurrencyRatesByDate(dateStr);
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Delete currency rates on 12/04/2024 (no records in the DB), for successful case")
    @SneakyThrows
    @Test
    void deleteRateCurrencyOnDate_shouldReturnResponseEntityEmpty_and200StatusCode() {
        LocalDate localDate = LocalDate.now().minusDays(2);
        String dateStr = FORMATTER.format(localDate);
        String messageStr = "42 records have been deleted from the DB";
        DeleteMessage message = new DeleteMessage(localDate, messageStr);

        when(currencyService.deleteCurrencyRatesByDate(dateStr)).thenReturn(message);

        ResponseEntity<DeleteMessage> responseEntity = currencyController.deleteRateCurrencyOnDate(dateStr);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(localDate, responseEntity.getBody().getDate());
        assertEquals(messageStr, responseEntity.getBody().getMessage());
        verify(currencyService, times(1)).deleteCurrencyRatesByDate(dateStr);
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Delete currency rates on 12/04/2024, when data has incorrect format")
    @SneakyThrows
    @Test
    void deleteRateCurrencyOnDate_shouldReturnException_andIncorrectDateFormat() {
        String dateStr = "2024-04-12";

        when(currencyService.deleteCurrencyRatesByDate(dateStr))
                .thenThrow(DateFormatException.getException());

        GenericSystemException ex = assertThrows(GenericSystemException.class, () ->
                currencyController.deleteRateCurrencyOnDate(dateStr));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals("Incorrect date format, to get a correct answer, the date must be in this format: dd-mm-yyyy", ex.getMessage());
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Delete currency rates on 01/01/1996, when data is deprecated (until 06/01/1996)")
    @SneakyThrows
    @Test
    void deleteRateCurrencyOnDate_shouldReturnException_andIncorrectDateException_VeryOldDate() {
        String dateStr = "01-01-1996";
        String message = String.format("Check your date-param, to get a correct answer, " +
                "the date must be between 06-01-1996 and %s", FORMATTER.format(LocalDate.now()));

        when(currencyService.deleteCurrencyRatesByDate(dateStr))
                .thenThrow(IncorrectDateException.getException());

        GenericSystemException ex = assertThrows(GenericSystemException.class, () ->
                currencyController.deleteRateCurrencyOnDate(dateStr));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals(message, ex.getMessage());
        verifyNoMoreInteractions(currencyService);
    }

    @DisplayName("Delete currency rates on 01/01/1996, when data is a future date (after tomorrow)")
    @SneakyThrows
    @Test
    void deleteRateCurrencyOnDate_shouldReturnException_andIncorrectDateException_FutureDate() {
        String dateStr = FORMATTER.format(LocalDate.now().plusDays(2));
        String message = String.format("Check your date-param, to get a correct answer, " +
                "the date must be between 06-01-1996 and %s", FORMATTER.format(LocalDate.now()));

        when(currencyService.deleteCurrencyRatesByDate(dateStr))
                .thenThrow(IncorrectDateException.getException());

        GenericSystemException ex = assertThrows(GenericSystemException.class, () ->
                currencyController.deleteRateCurrencyOnDate(dateStr));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals(message, ex.getMessage());
        verifyNoMoreInteractions(currencyService);
    }
}