package Esame.Back_End.Esame.Back_End.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO {
    @Email(message = "Email should be valid")
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
}

