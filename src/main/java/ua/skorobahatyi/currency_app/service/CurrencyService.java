package ua.skorobahatyi.currency_app.service;

import ua.skorobahatyi.currency_app.entity.dto.CurrencyResponseDto;
import ua.skorobahatyi.currency_app.entity.dto.DeleteMessage;
import ua.skorobahatyi.currency_app.exception.GenericSystemException;

public interface CurrencyService {
    CurrencyResponseDto findCurrencyRatesByDate(String dateStr) throws GenericSystemException;

    DeleteMessage deleteCurrencyRatesByDate(String dateStr) throws GenericSystemException;
}
