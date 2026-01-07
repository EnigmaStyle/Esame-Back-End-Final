package Esame.Back_End.Esame.Back_End.service;

import Esame.Back_End.Esame.Back_End.dto.BookingDTO;
import Esame.Back_End.Esame.Back_End.exception.BadRequestException;
import Esame.Back_End.Esame.Back_End.exception.ResourceNotFoundException;
import Esame.Back_End.Esame.Back_End.model.Booking;
import Esame.Back_End.Esame.Back_End.model.Customer;
import Esame.Back_End.Esame.Back_End.model.Screening;
import Esame.Back_End.Esame.Back_End.model.Seat;
import Esame.Back_End.Esame.Back_End.repository.BookingRepository;
import Esame.Back_End.Esame.Back_End.repository.CustomerRepository;
import Esame.Back_End.Esame.Back_End.repository.ScreeningRepository;
import Esame.Back_End.Esame.Back_End.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
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
    private final MailgunService mailgunService;
    
    public BookingService(BookingRepository bookingRepository, CustomerRepository customerRepository,
                         ScreeningRepository screeningRepository, SeatRepository seatRepository,
                         MailgunService mailgunService) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
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
    
    @Transactional
    public BookingDTO createBooking(BookingDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        
        Screening screening = screeningRepository.findById(dto.getScreeningId())
            .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + dto.getScreeningId()));
        
        if (!screening.getIsActive()) {
            throw new BadRequestException("Screening is not active");
        }
        
        Set<Seat> seats = new java.util.HashSet<>(seatRepository.findSeatsByIdsAndHall(dto.getSeatIds(), screening.getCinemaHall().getId()));
        
        if (seats.size() != dto.getSeatIds().size()) {
            throw new BadRequestException("Some seats are invalid or do not belong to this cinema hall");
        }
        
        // Check if seats are already booked
        List<Seat> availableSeats = seatRepository.findAvailableSeatsForScreening(
            screening.getCinemaHall().getId(), screening.getId());
        Set<Long> availableSeatIds = availableSeats.stream().map(Seat::getId).collect(Collectors.toSet());
        
        for (Long seatId : dto.getSeatIds()) {
            if (!availableSeatIds.contains(seatId)) {
                throw new BadRequestException("Seat with id " + seatId + " is already booked");
            }
        }
        
        BigDecimal totalAmount = screening.getTicketPrice().multiply(BigDecimal.valueOf(seats.size()));
        
        Booking booking = Booking.builder()
            .customer(customer)
            .screening(screening)
            .totalAmount(totalAmount)
            .status(Booking.BookingStatus.CONFIRMED)
            .build();
        
        booking.setSeats(seats);
        Booking saved = bookingRepository.save(booking);
        
        // Send confirmation email via Mailgun
        sendBookingConfirmationEmail(saved, customer, screening, seats);
        
        return convertToDTO(saved);
    }
    
    /**
     * Sends booking confirmation email via Mailgun API
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
            // Log error but don't fail the booking
            logger.error("Failed to send booking confirmation email: {}", e.getMessage());
        }
    }
    
    @Transactional
    public BookingDTO cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);
        
        // Send cancellation email via Mailgun
        sendBookingCancellationEmail(updated);
        
        return convertToDTO(updated);
    }
    
    /**
     * Sends booking cancellation email via Mailgun API
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
            // Log error but don't fail the cancellation
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

