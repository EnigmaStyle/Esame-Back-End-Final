package Esame.Back_End.Esame.Back_End.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String bookingCode;
    
    @Column(nullable = false)
    private LocalDateTime bookingDate;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;
    
    @ManyToMany
    @JoinTable(
        name = "booking_seats",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    @Builder.Default
    private Set<Seat> seats = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
        if (bookingCode == null || bookingCode.isEmpty()) {
            bookingCode = generateBookingCode();
        }
    }
    
    private String generateBookingCode() {
        return "BK" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    public enum BookingStatus {
        CONFIRMED, CANCELLED, COMPLETED
    }
}

