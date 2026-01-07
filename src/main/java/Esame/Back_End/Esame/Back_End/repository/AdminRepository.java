package Esame.Back_End.Esame.Back_End.repository;

import Esame.Back_End.Esame.Back_End.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}

