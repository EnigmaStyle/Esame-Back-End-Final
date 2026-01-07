package Esame.Back_End.Esame.Back_End.service;

import Esame.Back_End.Esame.Back_End.dto.BookingDTO;
import Esame.Back_End.Esame.Back_End.exception.BadRequestException;
import Esame.Back_End.Esame.Back_End.exception.ResourceNotFoundException;
import Esame.Back_End.Esame.Back_End.model.BookedSeat;
import Esame.Back_End.Esame.Back_End.model.Booking;
import Esame.Back_End.Esame.Back_End.model.Customer;
import Esame.Back_End.Esame.Back_End.model.Screening;
import Esame.Back_End.Esame.Back_End.model.Seat;
import Esame.Back_End.Esame.Back_End.repository.BookedSeatRepository;
import Esame.Back_End.Esame.Back_End.repository.BookingRepository;
import Esame.Back_End.Esame.Back_End.repository.CustomerRepository;
import Esame.Back_End.Esame.Back_End.repository.ScreeningRepository;
import Esame.Back_End.Esame.Back_End.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final MailgunService mailgunService;
    
    public BookingService(BookingRepository bookingRepository, CustomerRepository customerRepository,
                         ScreeningRepository screeningRepository, SeatRepository seatRepository,
                         BookedSeatRepository bookedSeatRepository, MailgunService mailgunService) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.mailgunService = mailgunService;
    }
    
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return convertToDTO(booking);
    }
    
    public BookingDTO getBookingByCode(String code) {
        Booking booking = bookingRepository.findByBookingCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with code: " + code));
        return convertToDTO(booking);
    }
    
    public List<BookingDTO> getBookingsByCustomerId(Long customerId) {
        return bookingRepository.findByCustomerId(customerId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Crea una nuova prenotazione con DOPPIA PROTEZIONE contro race condition:
     * 
     * 1. LOCKING PESSIMISTICO: Serializza l'accesso ai posti durante la transazione
     * 2. VINCOLO UNIQUE DB: La tabella booked_seats ha un vincolo UNIQUE su (screening_id, seat_id)
     *    che impedisce fisicamente la doppia prenotazione anche se il lock fallisce
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingDTO createBooking(BookingDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        
        Screening screening = screeningRepository.findById(dto.getScreeningId())
            .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + dto.getScreeningId()));
        
        if (!screening.getIsActive()) {
            throw new BadRequestException("Screening is not active");
        }
        
        // STEP 1: Verifica veloce se i posti sono già prenotati (ottimizzazione)
        if (bookedSeatRepository.existsAnyBookedSeatForScreening(screening.getId(), dto.getSeatIds())) {
            throw new BadRequestException("Uno o più posti sono già stati prenotati da un altro utente");
        }
        
        // STEP 2: LOCK PESSIMISTICO sui posti richiesti
        Set<Seat> seats = new java.util.HashSet<>(
            seatRepository.findSeatsByIdsAndHallWithLock(dto.getSeatIds(), screening.getCinemaHall().getId())
        );
        
        if (seats.size() != dto.getSeatIds().size()) {
            throw new BadRequestException("Some seats are invalid or do not belong to this cinema hall");
        }
        
        // STEP 3: Verifica disponibilità con LOCK
        List<Seat> availableSeats = seatRepository.findAvailableSeatsForScreeningWithLock(
            screening.getCinemaHall().getId(), screening.getId());
        Set<Long> availableSeatIds = availableSeats.stream().map(Seat::getId).collect(Collectors.toSet());
        
        for (Long seatId : dto.getSeatIds()) {
            if (!availableSeatIds.contains(seatId)) {
                logger.warn("Attempted to book unavailable seat {} for screening {}", seatId, screening.getId());
                throw new BadRequestException("Il posto con id " + seatId + " è già stato prenotato da un altro utente");
            }
        }
        
        BigDecimal totalAmount = screening.getTicketPrice().multiply(BigDecimal.valueOf(seats.size()));
        
        // STEP 4: Crea la prenotazione
        Booking booking = Booking.builder()
            .customer(customer)
            .screening(screening)
            .totalAmount(totalAmount)
            .status(Booking.BookingStatus.CONFIRMED)
            .build();
        
        booking.setSeats(seats);
        
        try {
            Booking saved = bookingRepository.save(booking);
            
            // STEP 5: Registra i posti nella tabella booked_seats (VINCOLO UNIQUE)
            // Se un altro utente ha già prenotato, il database lancia DataIntegrityViolationException
            List<BookedSeat> bookedSeats = new ArrayList<>();
            for (Seat seat : seats) {
                BookedSeat bookedSeat = BookedSeat.builder()
                    .booking(saved)
                    .screening(screening)
                    .seat(seat)
                    .build();
                bookedSeats.add(bookedSeat);
            }
            bookedSeatRepository.saveAll(bookedSeats);
            
            logger.info("Booking {} created successfully for customer {} - {} seats", 
                saved.getBookingCode(), customer.getEmail(), seats.size());
            
            // Invia email di conferma SOLO al cliente che ha effettuato la prenotazione
            sendBookingConfirmationEmail(saved, customer, screening, seats);
            
            return convertToDTO(saved);
            
        } catch (DataIntegrityViolationException e) {
            // Il vincolo UNIQUE ha catturato una race condition
            logger.error("Race condition detected: seat already booked for screening {}", screening.getId());
            throw new BadRequestException("Uno o più posti sono stati prenotati da un altro utente. Riprova.");
        }
    }
    
    /**
     * Invia email di conferma prenotazione via Mailgun API
     * SOLO al cliente che ha effettuato la prenotazione
     */
    private void sendBookingConfirmationEmail(Booking booking, Customer customer, Screening screening, Set<Seat> seats) {
        try {
            String customerName = customer.getFirstName() + " " + customer.getLastName();
            String movieTitle = screening.getMovie().getTitle();
            String screeningDate = screening.getStartTime().toLocalDate().format(DATE_FORMATTER);
            String screeningTime = screening.getStartTime().toLocalTime().format(TIME_FORMATTER);
            String seatsStr = seats.stream()
                .map(s -> s.getRowNumber() + "-" + s.getSeatNumber())
                .collect(Collectors.joining(", "));
            String totalAmount = booking.getTotalAmount().toString();
            
            mailgunService.sendBookingConfirmation(
                customer.getEmail(),
                customerName,
                booking.getBookingCode(),
                movieTitle,
                screeningDate,
                screeningTime,
                seatsStr,
                totalAmount
            );
            
            logger.info("Booking confirmation email sent to: {}", customer.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email: {}", e.getMessage());
        }
    }
    
    @Transactional
    public BookingDTO cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);
        
        // IMPORTANTE: Elimina i BookedSeat per consentire la ri-prenotazione dei posti.
        // Il vincolo UNIQUE su (screening_id, seat_id) impedirebbe nuove prenotazioni
        // se i record non venissero rimossi. Lo storico è preservato nella tabella booking
        // con status CANCELLED e la relazione booking_seats tramite @ManyToMany.
        bookedSeatRepository.deleteByBookingId(id);
        logger.info("BookedSeats deleted for cancelled booking {}", booking.getBookingCode());
        
        // Invia email di cancellazione SOLO al cliente proprietario della prenotazione
        sendBookingCancellationEmail(updated);
        
        return convertToDTO(updated);
    }
    
    /**
     * Invia email di cancellazione prenotazione via Mailgun API
     * SOLO al cliente proprietario della prenotazione
     */
    private void sendBookingCancellationEmail(Booking booking) {
        try {
            Customer customer = booking.getCustomer();
            String customerName = customer.getFirstName() + " " + customer.getLastName();
            String movieTitle = booking.getScreening().getMovie().getTitle();
            
            mailgunService.sendBookingCancellation(
                customer.getEmail(),
                customerName,
                booking.getBookingCode(),
                movieTitle
            );
            
            logger.info("Booking cancellation email sent to: {}", customer.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send booking cancellation email: {}", e.getMessage());
        }
    }
    
    public Long countBookedSeatsByScreening(Long screeningId) {
        return bookingRepository.countBookedSeatsByScreeningId(screeningId);
    }
    
    public BigDecimal calculateRevenueByScreening(Long screeningId) {
        BigDecimal revenue = bookingRepository.calculateTotalRevenueByScreeningId(screeningId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    public BigDecimal calculateRevenueByMovie(Long movieId) {
        BigDecimal revenue = bookingRepository.calculateTotalRevenueByMovieId(movieId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingCode(booking.getBookingCode());
        dto.setBookingDate(booking.getBookingDate());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus().name());
        dto.setCustomerId(booking.getCustomer().getId());
        dto.setScreeningId(booking.getScreening().getId());
        dto.setSeatIds(booking.getSeats().stream().map(Seat::getId).collect(Collectors.toSet()));
        return dto;
    }
}
