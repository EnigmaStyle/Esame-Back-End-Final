package Esame.Back_End.Esame.Back_End.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class per la sanitizzazione degli input utente.
 * Previene attacchi XSS e injection.
 */
@Component
public class InputSanitizer {
    
    // Pattern per rilevare potenziali attacchi XSS
    private static final Pattern XSS_SCRIPT_PATTERN = Pattern.compile(
        "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern XSS_EVENT_PATTERN = Pattern.compile(
        "on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern XSS_JAVASCRIPT_PATTERN = Pattern.compile(
        "javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern XSS_EXPRESSION_PATTERN = Pattern.compile(
        "expression\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern XSS_VBSCRIPT_PATTERN = Pattern.compile(
        "vbscript:", Pattern.CASE_INSENSITIVE);
    
    // Pattern per caratteri HTML pericolosi
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    
    /**
     * Sanitizza una stringa rimuovendo potenziali script XSS.
     * @param input Stringa da sanitizzare
     * @return Stringa sanitizzata
     */
    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String sanitized = input;
        
        // Rimuovi tag script
        sanitized = XSS_SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Rimuovi event handlers (onclick, onload, etc.)
        sanitized = XSS_EVENT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Rimuovi javascript: protocol
        sanitized = XSS_JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Rimuovi expression()
        sanitized = XSS_EXPRESSION_PATTERN.matcher(sanitized).replaceAll("");
        
        // Rimuovi vbscript:
        sanitized = XSS_VBSCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        return sanitized.trim();
    }
    
    /**
     * Sanitizza rimuovendo tutti i tag HTML.
     * Utile per campi di testo puro.
     */
    public String stripHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return HTML_TAG_PATTERN.matcher(input).replaceAll("").trim();
    }
    
    /**
     * Escape caratteri HTML speciali.
     * Converte < > & " ' in entità HTML.
     */
    public String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
    
    /**
     * Verifica se una stringa contiene potenziali attacchi XSS.
     * @return true se la stringa è potenzialmente pericolosa
     */
    public boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        return XSS_SCRIPT_PATTERN.matcher(input).find() ||
               XSS_EVENT_PATTERN.matcher(input).find() ||
               XSS_JAVASCRIPT_PATTERN.matcher(input).find() ||
               XSS_EXPRESSION_PATTERN.matcher(input).find() ||
               XSS_VBSCRIPT_PATTERN.matcher(input).find();
    }
    
    /**
     * Sanitizza email rimuovendo caratteri non validi.
     */
    public String sanitizeEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        // Rimuovi spazi e caratteri non validi per email
        return email.trim().toLowerCase().replaceAll("[^a-z0-9.@_+-]", "");
    }
    
    /**
     * Sanitizza nome/cognome.
     * Permette solo lettere, spazi, apostrofi e trattini.
     */
    public String sanitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.trim().replaceAll("[^\\p{L}\\s'-]", "");
    }
}
