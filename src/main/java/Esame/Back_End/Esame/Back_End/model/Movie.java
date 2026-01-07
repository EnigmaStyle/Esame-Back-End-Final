package Esame.Back_End.Esame.Back_End.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    private String genre;
    
    @Column(nullable = false)
    private Integer duration; // in minutes
    
    private LocalDate releaseDate;
    
    @Column(name = "poster_url")
    private String posterUrl;
    
    @Column(name = "backdrop_url")
    private String backdropUrl;
    
    private Double rating;
    
    private String director;
    
    @Column(name = "movie_cast", length = 1000)
    private String movieCast;
    
    @Column(name = "tmdb_id", unique = true)
    private Integer tmdbId;
    
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Screening> screenings = new ArrayList<>();
    
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
