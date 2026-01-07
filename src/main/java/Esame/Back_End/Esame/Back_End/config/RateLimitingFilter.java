package Esame.Back_End.Esame.Back_End.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Filter per prevenire attacchi brute-force e DoS.
 * Limita il numero di richieste per IP in un determinato intervallo di tempo.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    // Configurazione rate limiting
    private static final int MAX_REQUESTS_PER_MINUTE = 100; // Richieste generali
    private static final int MAX_AUTH_REQUESTS_PER_MINUTE = 10; // Richieste di autenticazione (login/register)
    private static final long TIME_WINDOW_MS = 60000; // 1 minuto
    
    // Cache per tracciare le richieste per IP
    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, RateLimitInfo> authRequestCounts = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String clientIp = getClientIP(request);
        String requestPath = request.getRequestURI();
        
        // Applica rate limiting più restrittivo per endpoint di autenticazione
        if (isAuthEndpoint(requestPath)) {
            if (!isAllowed(clientIp, authRequestCounts, MAX_AUTH_REQUESTS_PER_MINUTE)) {
                logger.warn("Rate limit exceeded for authentication from IP: {}", clientIp);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":429,\"message\":\"Too many authentication attempts. Please try again later.\",\"retryAfter\":60}");
                return;
            }
        }
        
        // Rate limiting generale
        if (!isAllowed(clientIp, requestCounts, MAX_REQUESTS_PER_MINUTE)) {
            logger.warn("Rate limit exceeded from IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Please try again later.\",\"retryAfter\":60}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isAuthEndpoint(String path) {
        return path.contains("/api/auth/login") || path.contains("/api/auth/register");
    }
    
    private boolean isAllowed(String clientIp, Map<String, RateLimitInfo> cache, int maxRequests) {
        long currentTime = System.currentTimeMillis();
        
        cache.compute(clientIp, (key, info) -> {
            if (info == null || currentTime - info.windowStart > TIME_WINDOW_MS) {
                // Nuova finestra temporale
                return new RateLimitInfo(currentTime, new AtomicInteger(1));
            } else {
                // Stessa finestra, incrementa contatore
                info.count.incrementAndGet();
                return info;
            }
        });
        
        RateLimitInfo info = cache.get(clientIp);
        return info.count.get() <= maxRequests;
    }
    
    private String getClientIP(HttpServletRequest request) {
        // Controlla header per proxy/load balancer
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Prendi il primo IP (client originale)
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private static class RateLimitInfo {
        long windowStart;
        AtomicInteger count;
        
        RateLimitInfo(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
