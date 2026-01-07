package Esame.Back_End.Esame.Back_End.service;

import Esame.Back_End.Esame.Back_End.dto.CinemaHallDTO;
import Esame.Back_End.Esame.Back_End.exception.ResourceNotFoundException;
import Esame.Back_End.Esame.Back_End.model.CinemaHall;
import Esame.Back_End.Esame.Back_End.model.Manager;
import Esame.Back_End.Esame.Back_End.model.Seat;
import Esame.Back_End.Esame.Back_End.repository.CinemaHallRepository;
import Esame.Back_End.Esame.Back_End.repository.ManagerRepository;
import Esame.Back_End.Esame.Back_End.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CinemaHallService {
    
    private final CinemaHallRepository cinemaHallRepository;
    private final ManagerRepository managerRepository;
    private final SeatRepository seatRepository;
    
    public CinemaHallService(CinemaHallRepository cinemaHallRepository, ManagerRepository managerRepository,
                            SeatRepository seatRepository) {
        this.cinemaHallRepository = cinemaHallRepository;
        this.managerRepository = managerRepository;
        this.seatRepository = seatRepository;
    }
    
    public List<CinemaHallDTO> getAllCinemaHalls() {
        return cinemaHallRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public CinemaHallDTO getCinemaHallById(Long id) {
        CinemaHall hall = cinemaHallRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cinema hall not found with id: " + id));
        return convertToDTO(hall);
    }
    
    @Transactional
    public CinemaHallDTO createCinemaHall(CinemaHallDTO dto) {
        Manager manager = managerRepository.findById(dto.getManagerId())
            .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + dto.getManagerId()));
        
        int totalSeats = dto.getTotalRows() * dto.getSeatsPerRow();
        
        CinemaHall hall = CinemaHall.builder()
            .name(dto.getName())
            .totalRows(dto.getTotalRows())
            .seatsPerRow(dto.getSeatsPerRow())
            .totalSeats(totalSeats)
            .description(dto.getDescription())
            .isActive(true)
            .manager(manager)
            .build();
        
        CinemaHall saved = cinemaHallRepository.save(hall);
        
        // Create seats
        createSeatsForHall(saved);
        
        return convertToDTO(saved);
    }
    
    private void createSeatsForHall(CinemaHall hall) {
        List<Seat> seats = new ArrayList<>();
        String[] rowLabels = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T"};
        
        for (int row = 0; row < hall.getTotalRows(); row++) {
            String rowNumber = rowLabels[row % rowLabels.length] + (row / rowLabels.length > 0 ? (row / rowLabels.length) : "");
            for (int seatNum = 1; seatNum <= hall.getSeatsPerRow(); seatNum++) {
                Seat seat = Seat.builder()
                    .rowNumber(rowNumber)
                    .seatNumber(seatNum)
                    .isVip(false)
                    .isActive(true)
                    .cinemaHall(hall)
                    .build();
                seats.add(seat);
            }
        }
        
        seatRepository.saveAll(seats);
    }
    
    @Transactional
    public CinemaHallDTO updateCinemaHall(Long id, CinemaHallDTO dto) {
        CinemaHall hall = cinemaHallRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cinema hall not found with id: " + id));
        
        if (dto.getName() != null) hall.setName(dto.getName());
        if (dto.getDescription() != null) hall.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) hall.setIsActive(dto.getIsActive());
        
        CinemaHall updated = cinemaHallRepository.save(hall);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void deleteCinemaHall(Long id) {
        if (!cinemaHallRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cinema hall not found with id: " + id);
        }
        cinemaHallRepository.deleteById(id);
    }
    
    private CinemaHallDTO convertToDTO(CinemaHall hall) {
        CinemaHallDTO dto = new CinemaHallDTO();
        dto.setId(hall.getId());
        dto.setName(hall.getName());
        dto.setTotalRows(hall.getTotalRows());
        dto.setSeatsPerRow(hall.getSeatsPerRow());
        dto.setTotalSeats(hall.getTotalSeats());
        dto.setDescription(hall.getDescription());
        dto.setIsActive(hall.getIsActive());
        dto.setManagerId(hall.getManager().getId());
        return dto;
    }
}

