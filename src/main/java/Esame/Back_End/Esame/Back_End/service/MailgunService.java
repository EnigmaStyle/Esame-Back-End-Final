package Esame.Back_End.Esame.Back_End.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending emails via Mailgun API.
 * Used to notify customers when new movie screenings are available.
 * 
 * NOTA: Con dominio sandbox, puoi inviare email SOLO a indirizzi autorizzati
 * su Mailgun Dashboard -> Sending -> Authorized Recipients
 */
@Service
public class MailgunService {
    
    private static final Logger logger = LoggerFactory.getLogger(MailgunService.class);
    
    @Value("${mailgun.api-key}")
    private String apiKey;
    
    @Value("${mailgun.domain}")
    private String domain;
    
    @Value("${mailgun.from-email}")
    private String fromEmail;
    
    @Value("${mailgun.from-name:Cinema Multi-Sala}")
    private String fromName;
    
    @Value("${mailgun.enabled:true}")
    private boolean enabled;
    
    private final RestTemplate restTemplate;
    private boolean isSandbox;
    
    public MailgunService() {
        this.restTemplate = new RestTemplate();
    }
    
    @PostConstruct
    public void init() {
        this.isSandbox = domain != null && domain.startsWith("sandbox");
        if (isSandbox) {
            logger.warn("⚠️ MAILGUN: Usando dominio SANDBOX - Le email possono essere inviate SOLO a destinatari autorizzati!");
            logger.warn("⚠️ Per autorizzare destinatari: https://app.mailgun.com/app/sending/domains/{}/authorized-recipients", domain);
        }
        if (!enabled) {
            logger.info("📧 MAILGUN: Servizio email DISABILITATO (mailgun.enabled=false)");
        } else {
            logger.info("📧 MAILGUN: Servizio email attivo con dominio: {}", domain);
        }
    }
    
    /**
     * Verifica la configurazione Mailgun
     * @return Map con status e dettagli
     */
    public Map<String, Object> verifyConfiguration() {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", enabled);
        result.put("domain", domain);
        result.put("fromEmail", fromEmail);
        result.put("isSandbox", isSandbox);
        
        if (!enabled) {
            result.put("status", "DISABLED");
            result.put("message", "Servizio email disabilitato");
            return result;
        }
        
        try {
            // Verifica le credenziali chiamando l'API domains
            String url = "https://api.mailgun.net/v3/domains/" + domain;
            
            HttpHeaders headers = new HttpHeaders();
            String auth = "api:" + apiKey;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, request, String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                result.put("status", "OK");
                result.put("message", "Configurazione Mailgun valida");
                if (isSandbox) {
                    result.put("warning", "Dominio sandbox: email inviate solo a destinatari autorizzati");
                }
            } else {
                result.put("status", "ERROR");
                result.put("message", "Errore verifica dominio: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            result.put("status", "ERROR");
            result.put("message", "Credenziali non valide o dominio inesistente: " + e.getMessage());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Errore connessione: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Invia una email di test
     * @param to destinatario (deve essere autorizzato se dominio sandbox)
     * @return Map con risultato
     */
    public Map<String, Object> sendTestEmail(String to) {
        Map<String, Object> result = new HashMap<>();
        result.put("to", to);
        
        if (!enabled) {
            result.put("success", false);
            result.put("message", "Servizio email disabilitato");
            return result;
        }
        
        String subject = "🎬 Test Email - Cinema Multi-Sala";
        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <div style="max-width: 500px; margin: 0 auto; background: #f5f5f5; padding: 30px; border-radius: 10px;">
                    <h1 style="color: #667eea;">✅ Test Riuscito!</h1>
                    <p>Se stai leggendo questa email, Mailgun è configurato correttamente.</p>
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                    <p style="color: #888; font-size: 12px;">Cinema Multi-Sala - Sistema di notifiche email</p>
                </div>
            </body>
            </html>
            """;
        
        boolean sent = sendEmail(to, subject, htmlBody);
        result.put("success", sent);
        
        if (sent) {
            result.put("message", "Email di test inviata con successo!");
        } else {
            result.put("message", "Invio fallito. Controlla i log per dettagli.");
            if (isSandbox) {
                result.put("hint", "Con dominio sandbox, assicurati che " + to + " sia un destinatario autorizzato su Mailgun");
            }
        }
        
        return result;
    }
    
    /**
     * Sends an email using Mailgun API
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param htmlBody HTML content of the email
     * @return true if email was sent successfully
     */
    public boolean sendEmail(String to, String subject, String htmlBody) {
        if (!enabled) {
            logger.debug("Email non inviata (servizio disabilitato): to={}, subject={}", to, subject);
            return false;
        }
        
        try {
            String url = "https://api.mailgun.net/v3/" + domain + "/messages";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String auth = "api:" + apiKey;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("from", fromName + " <" + fromEmail + ">");
            body.add("to", to);
            body.add("subject", subject);
            body.add("html", htmlBody);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            logger.debug("Invio email a {} via Mailgun...", to);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ Email inviata con successo a: {}", to);
                return true;
            } else {
                logger.error("❌ Invio email fallito. Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("❌ Errore Mailgun API per {}: {} - {}", to, e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401) {
                logger.error("   → API Key non valida!");
            } else if (e.getStatusCode().value() == 400 && isSandbox) {
                logger.error("   → Destinatario non autorizzato! Aggiungi {} ai destinatari autorizzati su Mailgun", to);
            }
            return false;
        } catch (Exception e) {
            logger.error("❌ Errore invio email a {}: {}", to, e.getMessage());
            return false;
        }
    }
    
    /**
     * Sends a new screening notification email
     */
    public boolean sendNewScreeningNotification(String to, String customerName, 
            String movieTitle, String screeningDate, String screeningTime, 
            String cinemaHall, String ticketPrice) {
        
        String subject = "🎬 Nuova Proiezione Disponibile: " + movieTitle;
        
        String htmlBody = buildNewScreeningEmailTemplate(
            customerName, movieTitle, screeningDate, screeningTime, cinemaHall, ticketPrice
        );
        
        return sendEmail(to, subject, htmlBody);
    }
    
    /**
     * Sends a booking confirmation email
     */
    public boolean sendBookingConfirmation(String to, String customerName,
            String bookingCode, String movieTitle, String screeningDate,
            String screeningTime, String seats, String totalAmount) {
        
        String subject = "✅ Conferma Prenotazione - " + bookingCode;
        
        String htmlBody = buildBookingConfirmationTemplate(
            customerName, bookingCode, movieTitle, screeningDate, 
            screeningTime, seats, totalAmount
        );
        
        return sendEmail(to, subject, htmlBody);
    }
    
    /**
     * Sends a booking cancellation email
     */
    public boolean sendBookingCancellation(String to, String customerName,
            String bookingCode, String movieTitle) {
        
        String subject = "❌ Prenotazione Cancellata - " + bookingCode;
        
        String htmlBody = buildBookingCancellationTemplate(customerName, bookingCode, movieTitle);
        
        return sendEmail(to, subject, htmlBody);
    }
    
    private String buildNewScreeningEmailTemplate(String customerName, String movieTitle,
            String screeningDate, String screeningTime, String cinemaHall, String ticketPrice) {
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .movie-info { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .info-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }
                    .info-label { font-weight: bold; color: #666; }
                    .cta-button { display: inline-block; background: #667eea; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎬 Nuova Proiezione!</h1>
                    </div>
                    <div class="content">
                        <p>Ciao <strong>%s</strong>,</p>
                        <p>Siamo lieti di informarti che è disponibile una nuova proiezione che potrebbe interessarti!</p>
                        
                        <div class="movie-info">
                            <h2 style="color: #667eea; margin-top: 0;">%s</h2>
                            <div class="info-row">
                                <span class="info-label">📅 Data:</span>
                                <span>%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">🕐 Orario:</span>
                                <span>%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">🎭 Sala:</span>
                                <span>%s</span>
                            </div>
                            <div class="info-row" style="border-bottom: none;">
                                <span class="info-label">💰 Prezzo:</span>
                                <span>€%s</span>
                            </div>
                        </div>
                        
                        <p>Non perdere questa occasione! I posti sono limitati.</p>
                        
                        <div class="footer">
                            <p>Cinema Multi-Sala - Il tuo cinema di fiducia</p>
                            <p>Questa email è stata inviata automaticamente. Per favore non rispondere.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(customerName, movieTitle, screeningDate, screeningTime, cinemaHall, ticketPrice);
    }
    
    private String buildBookingConfirmationTemplate(String customerName, String bookingCode,
            String movieTitle, String screeningDate, String screeningTime, 
            String seats, String totalAmount) {
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .booking-code { background: #333; color: white; padding: 15px; text-align: center; font-size: 24px; font-family: monospace; border-radius: 8px; margin: 20px 0; }
                    .booking-info { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .info-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }
                    .info-label { font-weight: bold; color: #666; }
                    .total { font-size: 20px; color: #11998e; font-weight: bold; }
                    .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Prenotazione Confermata!</h1>
                    </div>
                    <div class="content">
                        <p>Ciao <strong>%s</strong>,</p>
                        <p>La tua prenotazione è stata confermata con successo!</p>
                        
                        <div class="booking-code">
                            %s
                        </div>
                        <p style="text-align: center; color: #666;">Presenta questo codice alla cassa</p>
                        
                        <div class="booking-info">
                            <h3 style="color: #11998e; margin-top: 0;">Dettagli Prenotazione</h3>
                            <div class="info-row">
                                <span class="info-label">🎬 Film:</span>
                                <span>%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">📅 Data:</span>
                                <span>%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">🕐 Orario:</span>
                                <span>%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">💺 Posti:</span>
                                <span>%s</span>
                            </div>
                            <div class="info-row" style="border-bottom: none;">
                                <span class="info-label">💰 Totale:</span>
                                <span class="total">€%s</span>
                            </div>
                        </div>
                        
                        <p><strong>Ricorda:</strong> Presenta il codice di prenotazione alla cassa almeno 15 minuti prima dell'inizio della proiezione.</p>
                        
                        <div class="footer">
                            <p>Cinema Multi-Sala - Il tuo cinema di fiducia</p>
                            <p>Questa email è stata inviata automaticamente. Per favore non rispondere.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(customerName, bookingCode, movieTitle, screeningDate, screeningTime, seats, totalAmount);
    }
    
    private String buildBookingCancellationTemplate(String customerName, String bookingCode, String movieTitle) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #eb3349 0%%, #f45c43 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .cancelled-code { background: #ffebee; color: #c62828; padding: 15px; text-align: center; font-size: 20px; font-family: monospace; border-radius: 8px; margin: 20px 0; text-decoration: line-through; }
                    .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ Prenotazione Cancellata</h1>
                    </div>
                    <div class="content">
                        <p>Ciao <strong>%s</strong>,</p>
                        <p>La tua prenotazione è stata cancellata come richiesto.</p>
                        
                        <div class="cancelled-code">
                            %s
                        </div>
                        
                        <p><strong>Film:</strong> %s</p>
                        
                        <p>Se non hai richiesto tu questa cancellazione, contattaci immediatamente.</p>
                        
                        <div class="footer">
                            <p>Cinema Multi-Sala - Il tuo cinema di fiducia</p>
                            <p>Questa email è stata inviata automaticamente. Per favore non rispondere.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(customerName, bookingCode, movieTitle);
    }
}
