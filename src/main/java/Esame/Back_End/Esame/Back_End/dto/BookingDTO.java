package Esame.Back_End.Esame.Back_End.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private String bookingCode;
    private LocalDateTime bookingDate;
    private BigDecimal totalAmount;
    private String status;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Screening ID is required")
    private Long screeningId;
    
    @NotEmpty(message = "At least one seat is required")
    private Set<Long> seatIds;
}

