package Esame.Back_End.Esame.Back_End.service;

import Esame.Back_End.Esame.Back_End.dto.ScreeningDTO;
import Esame.Back_End.Esame.Back_End.dto.ScreeningSearchDTO;
import Esame.Back_End.Esame.Back_End.exception.BadRequestException;
import Esame.Back_End.Esame.Back_End.exception.ResourceNotFoundException;
import Esame.Back_End.Esame.Back_End.model.CinemaHall;
import Esame.Back_End.Esame.Back_End.model.Customer;
import Esame.Back_End.Esame.Back_End.model.Movie;
import Esame.Back_End.Esame.Back_End.model.Screening;
import Esame.Back_End.Esame.Back_End.repository.CinemaHallRepository;
import Esame.Back_End.Esame.Back_End.repository.CustomerRepository;
import Esame.Back_End.Esame.Back_End.repository.MovieRepository;
import Esame.Back_End.Esame.Back_End.repository.ScreeningRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScreeningService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScreeningService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final CustomerRepository customerRepository;
    private final MailgunService mailgunService;
    
    public ScreeningService(ScreeningRepository screeningRepository, MovieRepository movieRepository,
                           CinemaHallRepository cinemaHallRepository, CustomerRepository customerRepository,
                           MailgunService mailgunService) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.cinemaHallRepository = cinemaHallRepository;
        this.customerRepository = customerRepository;
        this.mailgunService = mailgunService;
    }
    
    
    public List<ScreeningDTO> getAllScreenings() {
        return screeningRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public ScreeningDTO getScreeningById(Long id) {
        Screening screening = screeningRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + id));
        return convertToDTO(screening);
    }
    
    @Transactional
    public ScreeningDTO createScreening(ScreeningDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + dto.getMovieId()));
        
        CinemaHall hall = cinemaHallRepository.findById(dto.getCinemaHallId())
            .orElseThrow(() -> new ResourceNotFoundException("Cinema hall not found with id: " + dto.getCinemaHallId()));
        
        if (dto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time cannot be in the past");
        }
        
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }
        
        Screening screening = Screening.builder()
            .startTime(dto.getStartTime())
            .endTime(dto.getEndTime())
            .ticketPrice(dto.getTicketPrice())
            .isActive(true)
            .movie(movie)
            .cinemaHall(hall)
            .build();
        
        Screening saved = screeningRepository.save(screening);
        
        // Send notification emails to all active customers via Mailgun
        notifyCustomersAboutNewScreening(saved, movie, hall);
        
        return convertToDTO(saved);
    }
    
    /**
     * Sends email notifications to all active customers about a new screening via Mailgun API
     */
    private void notifyCustomersAboutNewScreening(Screening screening, Movie movie, CinemaHall hall) {
        try {
            List<Customer> activeCustomers = customerRepository.findByIsActiveTrue();
            
            if (activeCustomers.isEmpty()) {
                logger.info("No active customers to notify about new screening");
                return;
            }
            
            String movieTitle = movie.getTitle();
            String screeningDate = screening.getStartTime().toLocalDate().format(DATE_FORMATTER);
            String screeningTime = screening.getStartTime().toLocalTime().format(TIME_FORMATTER);
            String cinemaHallName = hall.getName();
            String ticketPrice = screening.getTicketPrice().toString();
            
            int successCount = 0;
            int failCount = 0;
            
            for (Customer customer : activeCustomers) {
                String customerName = customer.getFirstName() + " " + customer.getLastName();
                
                boolean sent = mailgunService.sendNewScreeningNotification(
                    customer.getEmail(),
                    customerName,
                    movieTitle,
                    screeningDate,
                    screeningTime,
                    cinemaHallName,
                    ticketPrice
                );
                
                if (sent) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
            
            logger.info("New screening notifications sent: {} successful, {} failed", successCount, failCount);
            
        } catch (Exception e) {
            // Log error but don't fail the screening creation
            logger.error("Failed to send new screening notifications: {}", e.getMessage());
        }
    }
    
    @Transactional
    public ScreeningDTO updateScreening(Long id, ScreeningDTO dto) {
        Screening screening = screeningRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + id));
        
        if (dto.getStartTime() != null) screening.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) screening.setEndTime(dto.getEndTime());
        if (dto.getTicketPrice() != null) screening.setTicketPrice(dto.getTicketPrice());
        if (dto.getIsActive() != null) screening.setIsActive(dto.getIsActive());
        
        Screening updated = screeningRepository.save(screening);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void deleteScreening(Long id) {
        if (!screeningRepository.existsById(id)) {
            throw new ResourceNotFoundException("Screening not found with id: " + id);
        }
        screeningRepository.deleteById(id);
    }
    
    public List<ScreeningDTO> searchScreenings(ScreeningSearchDTO searchDTO) {
        LocalDateTime now = LocalDateTime.now();
        
        if (searchDTO.getStartDate() != null && searchDTO.getEndDate() != null) {
            return screeningRepository.findByDateRange(searchDTO.getStartDate(), searchDTO.getEndDate()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        }
        
        if (searchDTO.getGenre() != null) {
            return screeningRepository.findByGenreAndFutureDate(searchDTO.getGenre(), now).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        }
        
        if (searchDTO.getMovieId() != null) {
            return screeningRepository.findByMovieIdAndFutureDate(searchDTO.getMovieId(), now).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        }
        
        if (searchDTO.getCinemaHallId() != null) {
            return screeningRepository.findByCinemaHallIdAndFutureDate(searchDTO.getCinemaHallId(), now).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        }
        
        if (searchDTO.getOnlyAvailable() != null && searchDTO.getOnlyAvailable()) {
            return screeningRepository.findScreeningsWithAvailableSeats(now).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        }
        
        return screeningRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private ScreeningDTO convertToDTO(Screening screening) {
        ScreeningDTO dto = new ScreeningDTO();
        dto.setId(screening.getId());
        dto.setStartTime(screening.getStartTime());
        dto.setEndTime(screening.getEndTime());
        dto.setTicketPrice(screening.getTicketPrice());
        dto.setIsActive(screening.getIsActive());
        dto.setMovieId(screening.getMovie().getId());
        dto.setCinemaHallId(screening.getCinemaHall().getId());
        return dto;
    }
}

