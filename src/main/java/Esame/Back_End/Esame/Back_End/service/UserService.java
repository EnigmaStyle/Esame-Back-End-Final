package Esame.Back_End.Esame.Back_End.service;

import Esame.Back_End.Esame.Back_End.dto.UserProfileUpdateDTO;
import Esame.Back_End.Esame.Back_End.exception.ResourceNotFoundException;
import Esame.Back_End.Esame.Back_End.model.User;
import Esame.Back_End.Esame.Back_End.repository.UserRepository;
import Esame.Back_End.Esame.Back_End.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    
    public UserService(UserRepository userRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }
    
    public User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        return userDetails.getUser();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
    
    @Transactional
    public User updateProfile(UserProfileUpdateDTO dto) {
        User currentUser = getCurrentUser();
        
        if (dto.getFirstName() != null) {
            currentUser.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            currentUser.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(currentUser.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            currentUser.setEmail(dto.getEmail());
        }
        if (dto.getProfileImageUrl() != null) {
            currentUser.setProfileImageUrl(dto.getProfileImageUrl());
        }
        
        return userRepository.save(currentUser);
    }
    
    @Transactional
    public User updateProfileImage(String imageUrl) {
        User currentUser = getCurrentUser();
        currentUser.setProfileImageUrl(imageUrl);
        return userRepository.save(currentUser);
    }
}

