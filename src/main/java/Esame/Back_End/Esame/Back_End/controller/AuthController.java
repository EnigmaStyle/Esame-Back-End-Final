package Esame.Back_End.Esame.Back_End.controller;

import Esame.Back_End.Esame.Back_End.dto.JwtResponse;
import Esame.Back_End.Esame.Back_End.dto.LoginRequest;
import Esame.Back_End.Esame.Back_End.dto.RegisterRequest;
import Esame.Back_End.Esame.Back_End.security.JwtUtil;
import Esame.Back_End.Esame.Back_End.service.AuthService;
import Esame.Back_End.Esame.Back_End.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;
    
    public AuthController(AuthService authService, TokenBlacklistService tokenBlacklistService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        JwtResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Logout - Invalida il token JWT aggiungendolo alla blacklist
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                Date expiryDate = jwtUtil.extractExpiration(token);
                tokenBlacklistService.blacklistToken(token, expiryDate, "User logout");
                
                return ResponseEntity.ok(Map.of(
                    "message", "Logout effettuato con successo",
                    "status", "success"
                ));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Token non valido",
                    "status", "error"
                ));
            }
        }
        
        return ResponseEntity.badRequest().body(Map.of(
            "message", "Nessun token fornito",
            "status", "error"
        ));
    }
}
