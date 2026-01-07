package Esame.Back_End.Esame.Back_End.controller;

import Esame.Back_End.Esame.Back_End.dto.CinemaHallDTO;
import Esame.Back_End.Esame.Back_End.service.CinemaHallService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinema-halls")
@CrossOrigin(origins = "*")
public class CinemaHallController {
    
    private final CinemaHallService cinemaHallService;
    
    public CinemaHallController(CinemaHallService cinemaHallService) {
        this.cinemaHallService = cinemaHallService;
    }
    
    @GetMapping
    public ResponseEntity<List<CinemaHallDTO>> getAllCinemaHalls() {
        List<CinemaHallDTO> halls = cinemaHallService.getAllCinemaHalls();
        return ResponseEntity.ok(halls);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CinemaHallDTO> getCinemaHallById(@PathVariable Long id) {
        CinemaHallDTO hall = cinemaHallService.getCinemaHallById(id);
        return ResponseEntity.ok(hall);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<CinemaHallDTO> createCinemaHall(@Valid @RequestBody CinemaHallDTO dto) {
        CinemaHallDTO created = cinemaHallService.createCinemaHall(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<CinemaHallDTO> updateCinemaHall(@PathVariable Long id, @Valid @RequestBody CinemaHallDTO dto) {
        CinemaHallDTO updated = cinemaHallService.updateCinemaHall(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteCinemaHall(@PathVariable Long id) {
        cinemaHallService.deleteCinemaHall(id);
        return ResponseEntity.noContent().build();
    }
}

