package ua.skorobahatyi.currency_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "currencies")
@IdClass(CurrencyDateId.class)
public class Currency {
    @Id
    private LocalDate dateId;
    @Id
    private Integer currencyId;

    private String code;

    private String name;

    private BigDecimal rate;

    private Instant createdAt;
}
