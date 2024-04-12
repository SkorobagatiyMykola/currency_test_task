package ua.skorobahatyi.currency_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.skorobahatyi.currency_app.entity.Currency;
import ua.skorobahatyi.currency_app.entity.CurrencyDateId;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, CurrencyDateId> {
    List<Currency> findAllByDateId(LocalDate date);

    @Query(value = "SELECT distinct date_id FROM currencies ",nativeQuery = true)
    List<String> findDistinctDates();
}
