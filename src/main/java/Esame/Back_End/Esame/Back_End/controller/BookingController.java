package Esame.Back_End.Esame.Back_End.controller;

import Esame.Back_End.Esame.Back_End.dto.BookingDTO;
import Esame.Back_End.Esame.Back_End.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    
    private final BookingService bookingService;
    
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        BookingDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }
    
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BookingDTO> getBookingByCode(@PathVariable String code) {
        BookingDTO booking = bookingService.getBookingByCode(code);
        return ResponseEntity.ok(booking);
    }
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingDTO>> getBookingsByCustomerId(@PathVariable Long customerId) {
        List<BookingDTO> bookings = bookingService.getBookingsByCustomerId(customerId);
        return ResponseEntity.ok(bookings);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingDTO dto) {
        BookingDTO created = bookingService.createBooking(dto);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long id) {
        BookingDTO cancelled = bookingService.cancelBooking(id);
        return ResponseEntity.ok(cancelled);
    }
    
    @GetMapping("/screening/{screeningId}/booked-seats")
    public ResponseEntity<Long> countBookedSeatsByScreening(@PathVariable Long screeningId) {
        Long count = bookingService.countBookedSeatsByScreening(screeningId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/screening/{screeningId}/revenue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BigDecimal> getRevenueByScreening(@PathVariable Long screeningId) {
        BigDecimal revenue = bookingService.calculateRevenueByScreening(screeningId);
        return ResponseEntity.ok(revenue);
    }
    
    @GetMapping("/movie/{movieId}/revenue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BigDecimal> getRevenueByMovie(@PathVariable Long movieId) {
        BigDecimal revenue = bookingService.calculateRevenueByMovie(movieId);
        return ResponseEntity.ok(revenue);
    }
}

