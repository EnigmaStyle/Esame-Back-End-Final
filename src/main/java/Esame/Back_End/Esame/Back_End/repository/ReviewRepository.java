package Esame.Back_End.Esame.Back_End.repository;

import Esame.Back_End.Esame.Back_End.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    @Query("SELECT r FROM Review r WHERE r.movie.id = :movieId ORDER BY r.reviewDate DESC")
    List<Review> findByMovieId(@Param("movieId") Long movieId);
    
    @Query("SELECT r FROM Review r WHERE r.customer.id = :customerId ORDER BY r.reviewDate DESC")
    List<Review> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT r FROM Review r WHERE r.customer.id = :customerId AND r.movie.id = :movieId")
    Optional<Review> findByCustomerIdAndMovieId(@Param("customerId") Long customerId, @Param("movieId") Long movieId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id = :movieId")
    Double calculateAverageRatingByMovieId(@Param("movieId") Long movieId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie.id = :movieId")
    Long countReviewsByMovieId(@Param("movieId") Long movieId);
}

