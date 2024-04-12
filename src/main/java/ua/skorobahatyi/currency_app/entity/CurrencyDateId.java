package ua.skorobahatyi.currency_app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyDateId implements Serializable {
    private LocalDate dateId;
    private Integer currencyId;
}
