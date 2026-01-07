package Esame.Back_End.Esame.Back_End.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningSearchDTO {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String genre;
    private Long movieId;
    private Long cinemaHallId;
    private Boolean onlyAvailable;
}

