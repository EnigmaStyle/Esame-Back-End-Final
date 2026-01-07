package Esame.Back_End.Esame.Back_End.controller;

import Esame.Back_End.Esame.Back_End.dto.ScreeningDTO;
import Esame.Back_End.Esame.Back_End.dto.ScreeningSearchDTO;
import Esame.Back_End.Esame.Back_End.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {
    
    private final ScreeningService screeningService;
    
    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }
    
    @GetMapping
    public ResponseEntity<List<ScreeningDTO>> getAllScreenings() {
        List<ScreeningDTO> screenings = screeningService.getAllScreenings();
        return ResponseEntity.ok(screenings);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ScreeningDTO> getScreeningById(@PathVariable Long id) {
        ScreeningDTO screening = screeningService.getScreeningById(id);
        return ResponseEntity.ok(screening);
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<ScreeningDTO>> searchScreenings(@RequestBody ScreeningSearchDTO searchDTO) {
        List<ScreeningDTO> screenings = screeningService.searchScreenings(searchDTO);
        return ResponseEntity.ok(screenings);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ScreeningDTO> createScreening(@Valid @RequestBody ScreeningDTO dto) {
        ScreeningDTO created = screeningService.createScreening(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ScreeningDTO> updateScreening(@PathVariable Long id, @Valid @RequestBody ScreeningDTO dto) {
        ScreeningDTO updated = screeningService.updateScreening(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long id) {
        screeningService.deleteScreening(id);
        return ResponseEntity.noContent().build();
    }
}

