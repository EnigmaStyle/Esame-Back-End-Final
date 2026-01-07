package Esame.Back_End.Esame.Back_End.controller;

import Esame.Back_End.Esame.Back_End.dto.MovieDTO;
import Esame.Back_End.Esame.Back_End.service.CloudinaryService;
import Esame.Back_End.Esame.Back_End.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*")
public class MovieController {
    
    private final MovieService movieService;
    private final CloudinaryService cloudinaryService;
    
    public MovieController(MovieService movieService, CloudinaryService cloudinaryService) {
        this.movieService = movieService;
        this.cloudinaryService = cloudinaryService;
    }
    
    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        List<MovieDTO> movies = movieService.getAllMovies();
        return ResponseEntity.ok(movies);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }
    
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieDTO>> getMoviesByGenre(@PathVariable String genre) {
        List<MovieDTO> movies = movieService.getMoviesByGenre(genre);
        return ResponseEntity.ok(movies);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MovieDTO> createMovie(@Valid @RequestBody MovieDTO dto) {
        MovieDTO created = movieService.createMovie(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDTO dto) {
        MovieDTO updated = movieService.updateMovie(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/poster")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MovieDTO> uploadMoviePoster(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadMoviePoster(file);
            MovieDTO dto = new MovieDTO();
            dto.setPosterUrl(imageUrl);
            MovieDTO updated = movieService.updateMovie(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

