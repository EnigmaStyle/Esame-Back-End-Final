package Esame.Back_End.Esame.Back_End.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningDTO {
    private Long id;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Ticket price is required")
    @Positive(message = "Ticket price must be positive")
    private BigDecimal ticketPrice;
    
    private Boolean isActive;
    
    @NotNull(message = "Movie ID is required")
    private Long movieId;
    
    @NotNull(message = "Cinema hall ID is required")
    private Long cinemaHallId;
}

