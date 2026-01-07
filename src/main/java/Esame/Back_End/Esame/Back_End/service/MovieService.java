package Esame.Back_End.Esame.Back_End.service;

import Esame.Back_End.Esame.Back_End.dto.MovieDTO;
import Esame.Back_End.Esame.Back_End.exception.ResourceNotFoundException;
import Esame.Back_End.Esame.Back_End.model.Movie;
import Esame.Back_End.Esame.Back_End.repository.MovieRepository;
import Esame.Back_End.Esame.Back_End.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {
    
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    
    public MovieService(MovieRepository movieRepository, ReviewRepository reviewRepository) {
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
    }
    
    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public MovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return convertToDTO(movie);
    }
    
    @Transactional
    public MovieDTO createMovie(MovieDTO dto) {
        Movie movie = convertToEntity(dto);
        Movie saved = movieRepository.save(movie);
        return convertToDTO(saved);
    }
    
    @Transactional
    public MovieDTO updateMovie(Long id, MovieDTO dto) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        
        updateMovieFromDTO(movie, dto);
        Movie updated = movieRepository.save(movie);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }
    
    public List<MovieDTO> getMoviesByGenre(String genre) {
        return movieRepository.findByGenre(genre).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setGenre(movie.getGenre());
        dto.setDuration(movie.getDuration());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setBackdropUrl(movie.getBackdropUrl());
        dto.setRating(movie.getRating());
        dto.setDirector(movie.getDirector());
        dto.setCast(movie.getMovieCast());
        dto.setTmdbId(movie.getTmdbId());
        
        // Calculate average rating from reviews
        Double avgRating = reviewRepository.calculateAverageRatingByMovieId(movie.getId());
        if (avgRating != null) {
            dto.setRating(avgRating);
        }
        
        return dto;
    }
    
    private Movie convertToEntity(MovieDTO dto) {
        return Movie.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .genre(dto.getGenre())
            .duration(dto.getDuration())
            .releaseDate(dto.getReleaseDate())
            .posterUrl(dto.getPosterUrl())
            .backdropUrl(dto.getBackdropUrl())
            .rating(dto.getRating())
            .director(dto.getDirector())
            .movieCast(dto.getCast())
            .tmdbId(dto.getTmdbId())
            .build();
    }
    
    private void updateMovieFromDTO(Movie movie, MovieDTO dto) {
        if (dto.getTitle() != null) movie.setTitle(dto.getTitle());
        if (dto.getDescription() != null) movie.setDescription(dto.getDescription());
        if (dto.getGenre() != null) movie.setGenre(dto.getGenre());
        if (dto.getDuration() != null) movie.setDuration(dto.getDuration());
        if (dto.getReleaseDate() != null) movie.setReleaseDate(dto.getReleaseDate());
        if (dto.getPosterUrl() != null) movie.setPosterUrl(dto.getPosterUrl());
        if (dto.getBackdropUrl() != null) movie.setBackdropUrl(dto.getBackdropUrl());
        if (dto.getRating() != null) movie.setRating(dto.getRating());
        if (dto.getDirector() != null) movie.setDirector(dto.getDirector());
        if (dto.getCast() != null) movie.setMovieCast(dto.getCast());
    }
}

