package Esame.Back_End.Esame.Back_End.config;

import Esame.Back_End.Esame.Back_End.model.*;
import Esame.Back_End.Esame.Back_End.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DataLoader per popolare il database con dati di test.
 * ATTENZIONE: Attivo SOLO con profilo "dev" per evitare di creare
 * utenti con password note in ambiente di produzione (OWASP security).
 * 
 * Per attivare: -Dspring.profiles.active=dev
 * oppure: SPRING_PROFILES_ACTIVE=dev
 */
@Component
@Profile("dev")
public class DataLoader implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final ManagerRepository managerRepository;
    private final CustomerRepository customerRepository;
    private final MovieRepository movieRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final SeatRepository seatRepository;
    private final ScreeningRepository screeningRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DataLoader(UserRepository userRepository, AdminRepository adminRepository,
                     ManagerRepository managerRepository, CustomerRepository customerRepository,
                     MovieRepository movieRepository, CinemaHallRepository cinemaHallRepository,
                     SeatRepository seatRepository, ScreeningRepository screeningRepository,
                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.managerRepository = managerRepository;
        this.customerRepository = customerRepository;
        this.movieRepository = movieRepository;
        this.cinemaHallRepository = cinemaHallRepository;
        this.seatRepository = seatRepository;
        this.screeningRepository = screeningRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        loadUsers();
        Manager manager = managerRepository.findByEmail("manager@cinema.com").orElse(null);
        if (manager != null) {
            loadMovies();
            loadCinemaHalls(manager);
            loadScreenings();
        }
        printSummary();
    }
    
    private void loadUsers() {
        // Create Admin user if not exists
        if (!userRepository.existsByEmail("admin@cinema.com")) {
            Admin admin = Admin.builder()
                .email("admin@cinema.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("System")
                .adminLevel("SUPER")
                .build();
            adminRepository.save(admin);
            logger.info("✓ Admin user created");
        }
        
        // Create Manager user if not exists
        if (!userRepository.existsByEmail("manager@cinema.com")) {
            Manager manager = Manager.builder()
                .email("manager@cinema.com")
                .password(passwordEncoder.encode("manager123"))
                .firstName("Manager")
                .lastName("Cinema")
                .department("Operations")
                .build();
            managerRepository.save(manager);
            logger.info("✓ Manager user created");
        }
        
        // Create Customer users
        if (!userRepository.existsByEmail("customer@example.com")) {
            Customer customer = Customer.builder()
                .email("customer@example.com")
                .password(passwordEncoder.encode("customer123"))
                .firstName("Mario")
                .lastName("Rossi")
                .isActive(true)
                .build();
            customerRepository.save(customer);
            logger.info("✓ Customer user created");
        }
        
        if (!userRepository.existsByEmail("user@example.com")) {
            Customer customer2 = Customer.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("user123"))
                .firstName("Luigi")
                .lastName("Verdi")
                .isActive(true)
                .build();
            customerRepository.save(customer2);
            logger.info("✓ Additional customer created");
        }
    }
    
    private void loadMovies() {
        if (movieRepository.count() == 0) {
            List<Movie> movies = List.of(
                Movie.builder()
                    .title("Inception")
                    .description("A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.")
                    .genre("Sci-Fi")
                    .duration(148)
                    .releaseDate(LocalDate.of(2010, 7, 16))
                    .rating(8.8)
                    .director("Christopher Nolan")
                    .build(),
                Movie.builder()
                    .title("The Dark Knight")
                    .description("When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.")
                    .genre("Action")
                    .duration(152)
                    .releaseDate(LocalDate.of(2008, 7, 18))
                    .rating(9.0)
                    .director("Christopher Nolan")
                    .build(),
                Movie.builder()
                    .title("Interstellar")
                    .description("A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.")
                    .genre("Sci-Fi")
                    .duration(169)
                    .releaseDate(LocalDate.of(2014, 11, 7))
                    .rating(8.6)
                    .director("Christopher Nolan")
                    .build(),
                Movie.builder()
                    .title("Pulp Fiction")
                    .description("The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.")
                    .genre("Crime")
                    .duration(154)
                    .releaseDate(LocalDate.of(1994, 10, 14))
                    .rating(8.9)
                    .director("Quentin Tarantino")
                    .build(),
                Movie.builder()
                    .title("The Matrix")
                    .description("A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.")
                    .genre("Sci-Fi")
                    .duration(136)
                    .releaseDate(LocalDate.of(1999, 3, 31))
                    .rating(8.7)
                    .director("The Wachowskis")
                    .build(),
                Movie.builder()
                    .title("Forrest Gump")
                    .description("The presidencies of Kennedy and Johnson, the Vietnam War, the Watergate scandal and other historical events unfold from the perspective of an Alabama man with an IQ of 75.")
                    .genre("Drama")
                    .duration(142)
                    .releaseDate(LocalDate.of(1994, 7, 6))
                    .rating(8.8)
                    .director("Robert Zemeckis")
                    .build(),
                Movie.builder()
                    .title("The Godfather")
                    .description("The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant youngest son.")
                    .genre("Crime")
                    .duration(175)
                    .releaseDate(LocalDate.of(1972, 3, 24))
                    .rating(9.2)
                    .director("Francis Ford Coppola")
                    .build(),
                Movie.builder()
                    .title("Fight Club")
                    .description("An insomniac office worker and a devil-may-care soapmaker form an underground fight club that evolves into something much, much more.")
                    .genre("Drama")
                    .duration(139)
                    .releaseDate(LocalDate.of(1999, 10, 15))
                    .rating(8.8)
                    .director("David Fincher")
                    .build()
            );
            
            movieRepository.saveAll(movies);
            logger.info("✓ {} movies loaded", movies.size());
        }
    }
    
    private void loadCinemaHalls(Manager manager) {
        if (cinemaHallRepository.count() == 0) {
            // Sala 1 - Grande
            CinemaHall hall1 = CinemaHall.builder()
                .name("Sala 1 - IMAX")
                .totalRows(10)
                .seatsPerRow(15)
                .totalSeats(150)
                .description("Sala principale con schermo IMAX e audio Dolby Atmos")
                .isActive(true)
                .manager(manager)
                .build();
            cinemaHallRepository.save(hall1);
            createSeats(hall1);
            
            // Sala 2 - Media
            CinemaHall hall2 = CinemaHall.builder()
                .name("Sala 2 - Standard")
                .totalRows(8)
                .seatsPerRow(12)
                .totalSeats(96)
                .description("Sala standard con ottima acustica")
                .isActive(true)
                .manager(manager)
                .build();
            cinemaHallRepository.save(hall2);
            createSeats(hall2);
            
            // Sala 3 - Piccola VIP
            CinemaHall hall3 = CinemaHall.builder()
                .name("Sala 3 - VIP")
                .totalRows(5)
                .seatsPerRow(8)
                .totalSeats(40)
                .description("Sala VIP con poltrone reclinabili e servizio in sala")
                .isActive(true)
                .manager(manager)
                .build();
            cinemaHallRepository.save(hall3);
            createSeats(hall3);
            
            logger.info("✓ 3 cinema halls loaded with seats");
        }
    }
    
    private void createSeats(CinemaHall hall) {
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        for (int row = 0; row < hall.getTotalRows(); row++) {
            for (int seatNum = 1; seatNum <= hall.getSeatsPerRow(); seatNum++) {
                Seat seat = Seat.builder()
                    .rowNumber(rows[row])
                    .seatNumber(seatNum)
                    .cinemaHall(hall)
                    .isVip(row >= hall.getTotalRows() - 2) // Last 2 rows are VIP
                    .build();
                seatRepository.save(seat);
            }
        }
    }
    
    private void loadScreenings() {
        if (screeningRepository.count() == 0) {
            List<Movie> movies = movieRepository.findAll();
            List<CinemaHall> halls = cinemaHallRepository.findAll();
            
            if (movies.isEmpty() || halls.isEmpty()) return;
            
            LocalDateTime baseTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            
            // Create screenings for next 7 days
            for (int day = 0; day < 7; day++) {
                LocalDateTime dayBase = baseTime.plusDays(day);
                
                for (int i = 0; i < Math.min(movies.size(), halls.size()); i++) {
                    Movie movie = movies.get(i % movies.size());
                    CinemaHall hall = halls.get(i % halls.size());
                    
                    // Afternoon showing
                    createScreening(movie, hall, dayBase.plusHours(i * 3), BigDecimal.valueOf(10.50));
                    
                    // Evening showing
                    createScreening(movie, hall, dayBase.plusHours(6 + i * 3), BigDecimal.valueOf(12.50));
                }
            }
            
            logger.info("✓ Screenings loaded for next 7 days");
        }
    }
    
    private void createScreening(Movie movie, CinemaHall hall, LocalDateTime startTime, BigDecimal price) {
        Screening screening = Screening.builder()
            .movie(movie)
            .cinemaHall(hall)
            .startTime(startTime)
            .endTime(startTime.plusMinutes(movie.getDuration()))
            .ticketPrice(price)
            .isActive(true)
            .build();
        screeningRepository.save(screening);
    }
    
    private void printSummary() {
        logger.info("\n");
        logger.info("╔══════════════════════════════════════════════════════════════╗");
        logger.info("║           🎬 CINEMA MULTI-SALA - READY!                      ║");
        logger.info("╠══════════════════════════════════════════════════════════════╣");
        logger.info("║  📊 Data Summary:                                            ║");
        logger.info("║    • Users: {}                                               ║", userRepository.count());
        logger.info("║    • Movies: {}                                              ║", movieRepository.count());
        logger.info("║    • Cinema Halls: {}                                        ║", cinemaHallRepository.count());
        logger.info("║    • Seats: {}                                             ║", seatRepository.count());
        logger.info("║    • Screenings: {}                                          ║", screeningRepository.count());
        logger.info("╠══════════════════════════════════════════════════════════════╣");
        logger.info("║  🔐 Test Credentials:                                        ║");
        logger.info("║    • ADMIN:    admin@cinema.com / admin123                   ║");
        logger.info("║    • MANAGER:  manager@cinema.com / manager123               ║");
        logger.info("║    • CUSTOMER: customer@example.com / customer123            ║");
        logger.info("╠══════════════════════════════════════════════════════════════╣");
        logger.info("║  🌐 Access Points:                                           ║");
        logger.info("║    • Web UI: http://localhost:8080                           ║");
        logger.info("║    • API:    http://localhost:8080/api                       ║");
        logger.info("╚══════════════════════════════════════════════════════════════╝");
    }
}
