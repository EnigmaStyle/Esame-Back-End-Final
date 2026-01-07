package Esame.Back_End.Esame.Back_End.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entità per tracciare i posti prenotati per ogni proiezione.
 * Il vincolo UNIQUE su (screening_id, seat_id) previene COMPLETAMENTE
 * che lo stesso posto venga prenotato due volte per la stessa proiezione,
 * anche in caso di attacchi concorrenti.
 */
@Entity
@Table(name = "booked_seats", 
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_booked_seat_screening", 
               columnNames = {"screening_id", "seat_id"}
           )
       },
       indexes = {
           @Index(name = "idx_booked_seats_screening", columnList = "screening_id"),
           @Index(name = "idx_booked_seats_seat", columnList = "seat_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookedSeat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
}
