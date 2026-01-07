package Esame.Back_End.Esame.Back_End.repository;

import Esame.Back_End.Esame.Back_End.model.CinemaHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaHallRepository extends JpaRepository<CinemaHall, Long> {
    List<CinemaHall> findByIsActiveTrue();
    
    @Query("SELECT h FROM CinemaHall h WHERE h.manager.id = :managerId")
    List<CinemaHall> findByManagerId(@Param("managerId") Long managerId);
}

