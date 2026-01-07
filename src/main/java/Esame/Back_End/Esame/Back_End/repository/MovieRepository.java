package Esame.Back_End.Esame.Back_End.repository;

import Esame.Back_End.Esame.Back_End.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTmdbId(Integer tmdbId);
    List<Movie> findByGenre(String genre);
    
    @Query("SELECT m FROM Movie m WHERE m.genre = :genre ORDER BY m.rating DESC")
    List<Movie> findByGenreOrderByRatingDesc(@Param("genre") String genre);
    
    @Query("SELECT m FROM Movie m WHERE m.title LIKE %:title% ORDER BY m.title ASC")
    List<Movie> searchByTitle(@Param("title") String title);
}

