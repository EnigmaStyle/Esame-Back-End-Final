package Esame.Back_End.Esame.Back_End.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaHallDTO {
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Total rows is required")
    @Min(value = 1, message = "Total rows must be at least 1")
    private Integer totalRows;
    
    @NotNull(message = "Seats per row is required")
    @Min(value = 1, message = "Seats per row must be at least 1")
    private Integer seatsPerRow;
    
    private Integer totalSeats;
    private String description;
    private Boolean isActive;
    private Long managerId;
}

