package Esame.Back_End.Esame.Back_End.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger per eventi di sicurezza.
 * Traccia login, logout, tentativi falliti, e altre attività sensibili.
 */
@Component
public class SecurityAuditLogger {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log tentativo di login riuscito.
     */
    public void logSuccessfulLogin(String email, String ipAddress) {
        auditLogger.info("[LOGIN_SUCCESS] User: {} | IP: {} | Time: {}", 
            maskEmail(email), ipAddress, getCurrentTime());
    }
    
    /**
     * Log tentativo di login fallito.
     */
    public void logFailedLogin(String email, String ipAddress, String reason) {
        auditLogger.warn("[LOGIN_FAILED] User: {} | IP: {} | Reason: {} | Time: {}", 
            maskEmail(email), ipAddress, reason, getCurrentTime());
    }
    
    /**
     * Log registrazione nuovo utente.
     */
    public void logUserRegistration(String email, String userType, String ipAddress) {
        auditLogger.info("[USER_REGISTERED] User: {} | Type: {} | IP: {} | Time: {}", 
            maskEmail(email), userType, ipAddress, getCurrentTime());
    }
    
    /**
     * Log accesso non autorizzato.
     */
    public void logUnauthorizedAccess(String email, String resource, String ipAddress) {
        auditLogger.warn("[UNAUTHORIZED_ACCESS] User: {} | Resource: {} | IP: {} | Time: {}", 
            maskEmail(email), resource, ipAddress, getCurrentTime());
    }
    
    /**
     * Log tentativo di accesso con token invalido.
     */
    public void logInvalidToken(String ipAddress, String reason) {
        auditLogger.warn("[INVALID_TOKEN] IP: {} | Reason: {} | Time: {}", 
            ipAddress, reason, getCurrentTime());
    }
    
    /**
     * Log rate limit superato.
     */
    public void logRateLimitExceeded(String ipAddress, String endpoint) {
        auditLogger.warn("[RATE_LIMIT_EXCEEDED] IP: {} | Endpoint: {} | Time: {}", 
            ipAddress, endpoint, getCurrentTime());
    }
    
    /**
     * Log tentativo di SQL injection o XSS.
     */
    public void logSuspiciousInput(String ipAddress, String input, String fieldName) {
        auditLogger.error("[SUSPICIOUS_INPUT] IP: {} | Field: {} | Time: {}", 
            ipAddress, fieldName, getCurrentTime());
        // Non loggare l'input sospetto per evitare injection nei log
    }
    
    /**
     * Log cambio password.
     */
    public void logPasswordChange(String email, String ipAddress) {
        auditLogger.info("[PASSWORD_CHANGED] User: {} | IP: {} | Time: {}", 
            maskEmail(email), ipAddress, getCurrentTime());
    }
    
    /**
     * Log aggiornamento profilo.
     */
    public void logProfileUpdate(String email, String ipAddress) {
        auditLogger.info("[PROFILE_UPDATED] User: {} | IP: {} | Time: {}", 
            maskEmail(email), ipAddress, getCurrentTime());
    }
    
    /**
     * Log operazione admin.
     */
    public void logAdminAction(String adminEmail, String action, String targetResource, String ipAddress) {
        auditLogger.info("[ADMIN_ACTION] Admin: {} | Action: {} | Target: {} | IP: {} | Time: {}", 
            maskEmail(adminEmail), action, targetResource, ipAddress, getCurrentTime());
    }
    
    /**
     * Maschera l'email per privacy nei log.
     * esempio@test.com -> e****@test.com
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "unknown";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "****" + email.substring(atIndex);
    }
    
    private String getCurrentTime() {
        return LocalDateTime.now().format(formatter);
    }
}
