package ua.skorobahatyi.currency_app.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.RestTemplate;
import ua.skorobahatyi.currency_app.entity.Currency;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.StreamSupport;

public class HelperForDB {
    private static String nbuCurrencyUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    private static RestTemplate restTemplate = new RestTemplate();
    public static void main(String[] args) {
//        findMaxRateAndMaxDecimalScale();
//
//        List<Currency> currencies1= getCurrencyFromNBU(LocalDate.now());
//        List<Currency> currencies2= getCurrencyFromNBU(LocalDate.of(2024,02,02));
//        System.out.println(currencies1.toString());



        System.out.println("=================== THE END =======================");
    }

    private static void findMaxRateAndMaxDecimalScale() {
        var jsonResponse = restTemplate.getForObject(nbuCurrencyUrl, JsonNode.class);

        Double maxRate = StreamSupport.stream(jsonResponse.spliterator(), true)
                .map(el -> el.get("rate"))
                .mapToDouble(JsonNode::asDouble)
                .max()
                .orElse(0d);

        int maxDecimalPrecision = StreamSupport.stream(jsonResponse.spliterator(), true)
                .map(el -> el.get("rate"))
                .map(JsonNode::decimalValue)
                .mapToInt(BigDecimal::scale)
                .max().orElse(0);

        System.out.println("Max: " + maxRate);
        System.out.println("maxDecimalPrecision: " + maxDecimalPrecision);
    }

    private static List<Currency> getCurrencyFromNBU(LocalDate date) {
        String url = nbuCurrencyUrl + "&date=" + date.toString().replaceAll("-", "");

        var jsonResponse = restTemplate.getForObject(nbuCurrencyUrl, JsonNode.class);

        var currencies = StreamSupport.stream(jsonResponse.spliterator(), true)
                .map(el -> new Currency(date,
                        el.get("r030").asInt(),
                        el.get("cc").asText(),
                        el.get("txt").asText(),
                        el.get("rate").decimalValue(),
                        null
                ))
                .toList();

        return currencies;
    }

}
