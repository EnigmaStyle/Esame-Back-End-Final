package Esame.Back_End.Esame.Back_End.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotBlank(message = "Genre is required")
    private String genre;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;
    
    private LocalDate releaseDate;
    private String posterUrl;
    private String backdropUrl;
    private Double rating;
    private String director;
    private String cast;
    private Integer tmdbId;
}

