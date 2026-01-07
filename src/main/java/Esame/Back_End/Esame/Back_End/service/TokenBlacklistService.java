package Esame.Back_End.Esame.Back_End.service;

import Esame.Back_End.Esame.Back_End.model.TokenBlacklist;
import Esame.Back_End.Esame.Back_End.repository.TokenBlacklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class TokenBlacklistService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    
    private final TokenBlacklistRepository tokenBlacklistRepository;
    
    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }
    
    /**
     * Aggiunge un token alla blacklist
     */
    @Transactional
    public void blacklistToken(String token, Date expiryDate, String reason) {
        if (isTokenBlacklisted(token)) {
            return; // Token già in blacklist
        }
        
        LocalDateTime expiry = expiryDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        
        TokenBlacklist blacklistedToken = TokenBlacklist.builder()
            .token(token)
            .expiryDate(expiry)
            .reason(reason)
            .build();
        
        tokenBlacklistRepository.save(blacklistedToken);
        logger.info("Token blacklisted. Reason: {}", reason);
    }
    
    /**
     * Verifica se un token è nella blacklist
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
    
    /**
     * Pulizia automatica dei token scaduti ogni ora
     */
    @Scheduled(fixedRate = 3600000) // Ogni ora
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            logger.info("Cleaned up {} expired tokens from blacklist", deleted);
        }
    }
}
