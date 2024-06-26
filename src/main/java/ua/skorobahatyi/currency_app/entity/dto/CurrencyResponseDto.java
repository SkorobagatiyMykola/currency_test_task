package ua.skorobahatyi.currency_app.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyResponseDto {
    @JsonProperty("date")
    private LocalDate date;
    @JsonProperty("currencies")
    private List<CurrencyDto> currencyDtoList;
}
