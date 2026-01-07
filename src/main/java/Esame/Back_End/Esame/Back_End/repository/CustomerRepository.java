package Esame.Back_End.Esame.Back_End.repository;

import Esame.Back_End.Esame.Back_End.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * Finds all active customers for email notifications
     */
    List<Customer> findByIsActiveTrue();
}

