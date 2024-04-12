package ua.skorobahatyi.currency_app.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMessage {

    @JsonProperty("deletedDate")
    private LocalDate date;
    @JsonProperty("message")
    private String message;
}
