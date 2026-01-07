# 🔐 Security Documentation

## Panoramica Sicurezza

Questo progetto implementa un sistema di sicurezza multi-livello seguendo le best practice OWASP.

---

## 🛡️ Misure di Sicurezza Implementate

### 1. Autenticazione e Autorizzazione

| Componente | Implementazione |
|------------|-----------------|
| **Password Hashing** | BCrypt con strength 12 (4096 iterazioni) |
| **Token Auth** | JWT con HMAC-SHA256 |
| **Session** | Stateless (nessuna sessione server-side) |
| **RBAC** | 3 ruoli: ADMIN, MANAGER, CUSTOMER |

### 2. Password Policy

La password deve contenere:
- ✅ Minimo 8 caratteri
- ✅ Almeno 1 lettera maiuscola [A-Z]
- ✅ Almeno 1 lettera minuscola [a-z]
- ✅ Almeno 1 numero [0-9]
- ✅ Almeno 1 carattere speciale [@#$%^&+=!]
- ✅ Nessuno spazio

### 3. Protezione API

| Protezione | Descrizione |
|------------|-------------|
| **Rate Limiting** | Max 100 req/min generali, 10 req/min per auth |
| **CORS** | Domini specifici (no wildcard) |
| **JWT Expiration** | Token scade dopo 24 ore |
| **Input Validation** | Jakarta Validation su tutti i DTO |

### 4. Security Headers

```
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
Strict-Transport-Security: max-age=31536000 (solo HTTPS)
```

### 5. Protezione Input

| Tipo | Protezione |
|------|------------|
| **SQL Injection** | JPA/Hibernate con prepared statements |
| **XSS** | Input sanitization + escape HTML |
| **CSRF** | Disabilitato (API stateless con JWT) |

### 6. Logging di Sicurezza

Eventi tracciati:
- ✅ Login riusciti/falliti
- ✅ Registrazioni utenti
- ✅ Accessi non autorizzati
- ✅ Token invalidi
- ✅ Rate limit superato
- ✅ Input sospetti (XSS attempts)

---

## 📁 File di Configurazione

### application.properties.example
File template con placeholder per credenziali. **Mai committare credenziali reali!**

### .gitignore
Configurato per ignorare:
- File con credenziali (*.env, application-*.properties)
- Chiavi e certificati (*.pem, *.key, *.jks)
- File di log
- File temporanei

---

## 🔧 Configurazione per Produzione

### 1. JWT Secret
```bash
# Genera una chiave sicura:
openssl rand -base64 64
```

### 2. Database
```properties
spring.datasource.password=${DB_PASSWORD}
```

### 3. API Keys
```properties
cloudinary.api-key=${CLOUDINARY_API_KEY}
mailgun.api-key=${MAILGUN_API_KEY}
```

### 4. HTTPS
In produzione, sempre usare HTTPS:
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
```

---

## 🚨 Vulnerabilità Note e Mitigazioni

| Vulnerabilità | Mitigazione |
|---------------|-------------|
| Brute Force | Rate limiting (10 tentativi/min per login) |
| Session Hijacking | JWT stateless + short expiration |
| XSS | Input sanitization + CSP headers |
| SQL Injection | JPA prepared statements |
| CSRF | Disabilitato (API stateless) |
| Clickjacking | X-Frame-Options: DENY |
| Info Leakage | Error messages generici |

---

## 📊 Componenti di Sicurezza

```
src/main/java/Esame/Back_End/Esame/Back_End/
├── config/
│   ├── RateLimitingFilter.java    # Protezione brute-force
│   ├── SecurityAuditLogger.java   # Logging eventi sicurezza
│   └── SecurityHeadersFilter.java # HTTP security headers
├── security/
│   ├── SecurityConfig.java        # Configurazione Spring Security
│   ├── JwtAuthenticationFilter.java # Validazione JWT
│   ├── JwtUtil.java               # Generazione/parsing JWT
│   ├── UserDetailsImpl.java       # User principal
│   └── UserDetailsServiceImpl.java # Caricamento utenti
├── validation/
│   ├── InputSanitizer.java        # Sanitizzazione input
│   ├── PasswordValidator.java     # Policy password
│   └── ValidPassword.java         # Annotation validazione
└── exception/
    ├── GlobalExceptionHandler.java    # Error handling sicuro
    └── SecurityExceptionHandler.java  # Auth/access errors
```

---

## ✅ Checklist Sicurezza

- [x] BCrypt per password (strength 12)
- [x] JWT con firma HMAC-SHA256
- [x] 3 ruoli con permessi granulari
- [x] Rate limiting su tutti gli endpoint
- [x] Rate limiting più restrittivo su auth
- [x] Security headers completi
- [x] Input validation su tutti i DTO
- [x] Input sanitization (anti-XSS)
- [x] Error messages generici
- [x] Audit logging eventi sicurezza
- [x] CORS configurato (no wildcard)
- [x] Session stateless
- [x] .gitignore per file sensibili
- [x] application.properties.example per documentazione

---

## 📝 Note per Sviluppatori

1. **Mai loggare password** o dati sensibili
2. **Mai committare** credenziali reali
3. **Sempre validare** input prima di usarlo
4. **Sempre usare** prepared statements (JPA lo fa automaticamente)
5. **Sempre controllare** autorizzazioni con @PreAuthorize

---

## 🔗 Riferimenti

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT Best Practices](https://auth0.com/docs/secure/tokens/json-web-tokens/json-web-token-best-practices)
