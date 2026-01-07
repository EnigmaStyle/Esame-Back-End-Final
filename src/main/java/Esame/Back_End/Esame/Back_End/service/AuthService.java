package Esame.Back_End.Esame.Back_End.service;

import Esame.Back_End.Esame.Back_End.config.SecurityAuditLogger;
import Esame.Back_End.Esame.Back_End.dto.JwtResponse;
import Esame.Back_End.Esame.Back_End.dto.LoginRequest;
import Esame.Back_End.Esame.Back_End.dto.RegisterRequest;
import Esame.Back_End.Esame.Back_End.exception.BadRequestException;
import Esame.Back_End.Esame.Back_End.model.Admin;
import Esame.Back_End.Esame.Back_End.model.Customer;
import Esame.Back_End.Esame.Back_End.model.Manager;
import Esame.Back_End.Esame.Back_End.model.User;
import Esame.Back_End.Esame.Back_End.repository.AdminRepository;
import Esame.Back_End.Esame.Back_End.repository.CustomerRepository;
import Esame.Back_End.Esame.Back_End.repository.ManagerRepository;
import Esame.Back_End.Esame.Back_End.repository.UserRepository;
import Esame.Back_End.Esame.Back_End.security.JwtUtil;
import Esame.Back_End.Esame.Back_End.security.UserDetailsImpl;
import Esame.Back_End.Esame.Back_End.validation.InputSanitizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Servizio di autenticazione con logging di sicurezza e sanitizzazione input.
 */
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final ManagerRepository managerRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SecurityAuditLogger auditLogger;
    private final InputSanitizer inputSanitizer;
    
    public AuthService(UserRepository userRepository, AdminRepository adminRepository,
                      ManagerRepository managerRepository, CustomerRepository customerRepository,
                      PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                      JwtUtil jwtUtil, SecurityAuditLogger auditLogger, InputSanitizer inputSanitizer) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.managerRepository = managerRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.auditLogger = auditLogger;
        this.inputSanitizer = inputSanitizer;
    }
    
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        String clientIp = getClientIp();
        
        // Sanitizza input
        String email = inputSanitizer.sanitizeEmail(request.getEmail());
        String firstName = inputSanitizer.sanitizeName(request.getFirstName());
        String lastName = inputSanitizer.sanitizeName(request.getLastName());
        
        // Verifica input sospetti
        if (inputSanitizer.containsXss(request.getFirstName()) || 
            inputSanitizer.containsXss(request.getLastName())) {
            auditLogger.logSuspiciousInput(clientIp, "XSS attempt", "name fields");
            throw new BadRequestException("Invalid characters in input");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }
        
        User user;
        String userType = request.getUserType() != null ? request.getUserType().toUpperCase() : "CUSTOMER";
        
        switch (userType) {
            case "ADMIN":
                Admin admin = Admin.builder()
                    .email(email)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(firstName)
                    .lastName(lastName)
                    .adminLevel(request.getAdminLevel() != null ? request.getAdminLevel() : "STANDARD")
                    .build();
                user = adminRepository.save(admin);
                break;
            case "MANAGER":
                Manager manager = Manager.builder()
                    .email(email)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(firstName)
                    .lastName(lastName)
                    .department(request.getDepartment() != null ? request.getDepartment() : "GENERAL")
                    .build();
                user = managerRepository.save(manager);
                break;
            default:
                Customer customer = Customer.builder()
                    .email(email)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(firstName)
                    .lastName(lastName)
                    .isActive(true)
                    .build();
                user = customerRepository.save(customer);
        }
        
        // Log registrazione riuscita
        auditLogger.logUserRegistration(email, userType, clientIp);
        
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String token = jwtUtil.generateToken(userDetails);
        
        String role = user instanceof Admin ? "ADMIN" : 
                     (user instanceof Manager ? "MANAGER" : "CUSTOMER");
        
        return new JwtResponse(token, user.getId(), user.getEmail(), role);
    }
    
    public JwtResponse login(LoginRequest request) {
        String clientIp = getClientIp();
        String email = inputSanitizer.sanitizeEmail(request.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            User user = userDetails.getUser();
            String role = user instanceof Admin ? "ADMIN" : 
                         (user instanceof Manager ? "MANAGER" : "CUSTOMER");
            
            // Log login riuscito
            auditLogger.logSuccessfulLogin(email, clientIp);
            
            return new JwtResponse(token, user.getId(), user.getEmail(), role);
            
        } catch (BadCredentialsException e) {
            // Log tentativo fallito
            auditLogger.logFailedLogin(email, clientIp, "Invalid credentials");
            throw e;
        }
    }
    
    /**
     * Ottiene l'IP del client dalla richiesta corrente.
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Ignora errori nel recupero IP
        }
        return "unknown";
    }
}

