package Esame.Back_End.Esame.Back_End.controller;

import Esame.Back_End.Esame.Back_End.dto.UserProfileUpdateDTO;
import Esame.Back_End.Esame.Back_End.model.User;
import Esame.Back_End.Esame.Back_End.service.CloudinaryService;
import Esame.Back_End.Esame.Back_End.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    
    public UserController(UserService userService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        User updated = userService.updateProfile(dto);
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/me/profile-image")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<User> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadProfileImage(file);
            User updated = userService.updateProfileImage(imageUrl);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

