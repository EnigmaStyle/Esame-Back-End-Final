package Esame.Back_End.Esame.Back_End.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cinema_halls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaHall {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private Integer totalRows;
    
    @Column(nullable = false)
    private Integer seatsPerRow;
    
    @Column(nullable = false)
    private Integer totalSeats;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private Manager manager;
    
    @OneToMany(mappedBy = "cinemaHall", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Seat> seats = new HashSet<>();
    
    @OneToMany(mappedBy = "cinemaHall", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Screening> screenings = new HashSet<>();
}

