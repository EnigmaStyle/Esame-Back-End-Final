package Esame.Back_End.Esame.Back_End.repository;

import Esame.Back_End.Esame.Back_End.model.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByCinemaHallId(Long cinemaHallId);
    
    @Query("SELECT s FROM Seat s WHERE s.cinemaHall.id = :hallId AND s.isActive = true")
    List<Seat> findActiveSeatsByCinemaHallId(@Param("hallId") Long hallId);
    
    @Query("SELECT s FROM Seat s WHERE s.cinemaHall.id = :hallId AND s.id NOT IN " +
           "(SELECT bs.id FROM Booking b JOIN b.seats bs WHERE b.screening.id = :screeningId AND b.status = 'CONFIRMED')")
    List<Seat> findAvailableSeatsForScreening(@Param("hallId") Long hallId, @Param("screeningId") Long screeningId);
    
    /**
     * Trova i posti disponibili con LOCK PESSIMISTICO per prevenire race condition.
     * Usa questo metodo durante la creazione di prenotazioni.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.cinemaHall.id = :hallId AND s.id NOT IN " +
           "(SELECT bs.id FROM Booking b JOIN b.seats bs WHERE b.screening.id = :screeningId AND b.status = 'CONFIRMED')")
    List<Seat> findAvailableSeatsForScreeningWithLock(@Param("hallId") Long hallId, @Param("screeningId") Long screeningId);
    
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.cinemaHall.id = :hallId")
    List<Seat> findSeatsByIdsAndHall(@Param("seatIds") Set<Long> seatIds, @Param("hallId") Long hallId);
    
    /**
     * Trova i posti per ID con LOCK PESSIMISTICO per prevenire race condition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.cinemaHall.id = :hallId")
    List<Seat> findSeatsByIdsAndHallWithLock(@Param("seatIds") Set<Long> seatIds, @Param("hallId") Long hallId);
}
