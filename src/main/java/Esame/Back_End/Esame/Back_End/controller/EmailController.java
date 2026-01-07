package Esame.Back_End.Esame.Back_End.controller;

import Esame.Back_End.Esame.Back_End.service.MailgunService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller per gestire le operazioni email (Mailgun).
 * Solo ADMIN può accedere a questi endpoint.
 */
@RestController
@RequestMapping("/api/email")
@Validated
public class EmailController {
    
    private final MailgunService mailgunService;
    
    public EmailController(MailgunService mailgunService) {
        this.mailgunService = mailgunService;
    }
    
    /**
     * Verifica la configurazione Mailgun
     * GET /api/email/verify
     */
    @GetMapping("/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> verifyConfiguration() {
        Map<String, Object> result = mailgunService.verifyConfiguration();
        return ResponseEntity.ok(result);
    }
    
    /**
     * Invia una email di test
     * POST /api/email/test?to=email@example.com
     * 
     * NOTA: Con dominio sandbox, il destinatario deve essere autorizzato su Mailgun
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendTestEmail(
            @RequestParam @NotBlank @Email String to) {
        Map<String, Object> result = mailgunService.sendTestEmail(to);
        return ResponseEntity.ok(result);
    }
}
