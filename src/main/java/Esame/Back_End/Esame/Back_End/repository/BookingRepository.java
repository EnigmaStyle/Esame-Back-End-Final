package Esame.Back_End.Esame.Back_End.repository;

import Esame.Back_End.Esame.Back_End.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    
    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId ORDER BY b.bookingDate DESC")
    List<Booking> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT b FROM Booking b WHERE b.screening.id = :screeningId")
    List<Booking> findByScreeningId(@Param("screeningId") Long screeningId);
    
    @Query(value = "SELECT COUNT(*) FROM booking_seats bs " +
            "WHERE bs.booking_id IN (SELECT id FROM bookings WHERE screening_id = :screeningId)", nativeQuery = true)
    Long countBookedSeatsByScreeningId(@Param("screeningId") Long screeningId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.screening.id = :screeningId")
    Long countBookingsByScreeningId(@Param("screeningId") Long screeningId);
    
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.screening.id = :screeningId AND b.status = 'CONFIRMED'")
    BigDecimal calculateTotalRevenueByScreeningId(@Param("screeningId") Long screeningId);
    
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.screening.movie.id = :movieId AND b.status = 'CONFIRMED'")
    BigDecimal calculateTotalRevenueByMovieId(@Param("movieId") Long movieId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.screening.movie.id = :movieId AND b.status = 'CONFIRMED'")
    Long countBookingsByMovieId(@Param("movieId") Long movieId);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingDate >= :startDate AND b.bookingDate <= :endDate ORDER BY b.bookingDate DESC")
    List<Booking> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

