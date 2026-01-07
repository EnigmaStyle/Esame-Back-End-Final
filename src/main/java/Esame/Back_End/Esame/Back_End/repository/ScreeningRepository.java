package Esame.Back_End.Esame.Back_End.repository;

import Esame.Back_End.Esame.Back_End.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    
    @Query("SELECT s FROM Screening s WHERE s.startTime >= :startDate AND s.startTime <= :endDate AND s.isActive = true ORDER BY s.startTime ASC")
    List<Screening> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Screening s WHERE s.movie.genre = :genre AND s.isActive = true AND s.startTime >= :now ORDER BY s.startTime ASC")
    List<Screening> findByGenreAndFutureDate(@Param("genre") String genre, @Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.isActive = true AND s.startTime >= :now ORDER BY s.startTime ASC")
    List<Screening> findByMovieIdAndFutureDate(@Param("movieId") Long movieId, @Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Screening s WHERE s.cinemaHall.id = :hallId AND s.isActive = true AND s.startTime >= :now ORDER BY s.startTime ASC")
    List<Screening> findByCinemaHallIdAndFutureDate(@Param("hallId") Long hallId, @Param("now") LocalDateTime now);
    
    @Query(value = "SELECT s.*, " +
            "(SELECT COUNT(*) FROM bookings b WHERE b.screening_id = s.id) as booked_seats, " +
            "(SELECT h.total_seats FROM cinema_halls h WHERE h.id = s.cinema_hall_id) as total_seats " +
            "FROM screenings s " +
            "WHERE s.id = :screeningId", nativeQuery = true)
    Object findScreeningWithAvailableSeats(@Param("screeningId") Long screeningId);
    
    @Query("SELECT s FROM Screening s WHERE s.isActive = true AND s.startTime >= :now " +
           "AND (SELECT COUNT(b) FROM Booking b WHERE b.screening.id = s.id) < " +
           "(SELECT h.totalSeats FROM CinemaHall h WHERE h.id = s.cinemaHall.id) " +
           "ORDER BY s.startTime ASC")
    List<Screening> findScreeningsWithAvailableSeats(@Param("now") LocalDateTime now);
}

