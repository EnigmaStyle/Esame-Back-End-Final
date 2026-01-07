package Esame.Back_End.Esame.Back_End.repository;

import Esame.Back_End.Esame.Back_End.model.BookedSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {
    
    @Query("SELECT bs.seat.id FROM BookedSeat bs WHERE bs.screening.id = :screeningId AND bs.booking.status = 'CONFIRMED'")
    Set<Long> findBookedSeatIdsForScreening(@Param("screeningId") Long screeningId);
    
    @Query("SELECT COUNT(bs) > 0 FROM BookedSeat bs WHERE bs.screening.id = :screeningId AND bs.seat.id IN :seatIds AND bs.booking.status = 'CONFIRMED'")
    boolean existsAnyBookedSeatForScreening(@Param("screeningId") Long screeningId, @Param("seatIds") Set<Long> seatIds);
    
    List<BookedSeat> findByBookingId(Long bookingId);
    
    @Modifying
    @Query("DELETE FROM BookedSeat bs WHERE bs.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
