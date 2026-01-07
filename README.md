# Cinema Multi-Sala Management System

Sistema completo di gestione per un cinema multi-sala sviluppato con Spring Boot e PostgreSQL.

## 🖥️ Interfaccia Web

L'applicazione include un'interfaccia web minimale accessibile su:

```
http://localhost:8080
```

### Funzionalità UI:
- ✅ **Login/Registrazione** - Accesso e creazione account
- ✅ **Lista Film** - Visualizzazione film in programmazione
- ✅ **Proiezioni** - Elenco proiezioni con prenotazione
- ✅ **Prenotazioni** - Gestione prenotazioni personali
- ✅ **Profilo** - Visualizzazione dati utente

### Credenziali di Test (solo profilo dev):
| Ruolo | Email | Password |
|-------|-------|----------|
| CUSTOMER | customer@example.com | customer123 |
| MANAGER | manager@cinema.com | manager123 |
| ADMIN | admin@cinema.com | admin123 |

> ⚠️ **Nota**: Gli utenti di test vengono creati solo con profilo `dev` attivo

---

## 🎯 Caratteristiche Principali

- **Architettura a 3 livelli**: Model, Repository, Service, Controller
- **Sicurezza JWT**: Autenticazione e autorizzazione basata su token JWT
- **Ereditarietà Database**: Strategia JOINED per la gerarchia degli utenti
- **API Terze Parti**: Integrazione con Cloudinary (immagini) e Mailgun (email)
- **Query Complesse**: Filtri, ordinamento e aggregazioni con JPQL/SQL nativo
- **Validazione Completa**: Validazione DTO con Jakarta Validation
- **Gestione Errori**: Global Exception Handler centralizzato
- **Test Suite**: Test unitari per le funzionalità critiche (prenotazioni)

## 📋 Requisiti

- Java 21
- Maven 3.8+
- PostgreSQL 12+
- Account Cloudinary (per upload immagini)
- Account Mailgun (per invio email notifiche)

## 🗄️ Database Schema

Il sistema utilizza **12 tabelle** con ereditarietà:

### Tabelle Principali:
1. **users** (tabella base per User - abstract)
2. **admins** (sottoclasse User)
3. **managers** (sottoclasse User)
4. **customers** (sottoclasse User)
5. **movies** (Film)
6. **cinema_halls** (Sale cinematografiche)
7. **screenings** (Proiezioni)
8. **seats** (Posti a sedere)
9. **bookings** (Prenotazioni)
10. **booked_seats** (Tracciamento posti prenotati con vincolo UNIQUE)
11. **reviews** (Recensioni)
12. **booking_seats** (Tabella di join per prenotazioni-posti)

### Ereditarietà:
- **User** (classe astratta) con strategia `@Inheritance(strategy = InheritanceType.JOINED)`
- **Admin**, **Manager**, **Customer** estendono User

## 🔐 Sicurezza

L'applicazione implementa multiple layer di sicurezza:

- **Password Policy Forte**: Password devono contenere almeno 8 caratteri, maiuscole, minuscole, numeri e caratteri speciali
- **BCrypt**: Criptazione password con BCryptPasswordEncoder (strength 12) e salt automatico
- **JWT**: Token JWT per autenticazione stateless con scadenza configurata
- **Token Blacklist**: Sistema di invalidazione token con cleanup schedulato
- **RBAC**: 3 ruoli distinti (ADMIN, MANAGER, CUSTOMER) con controllo accesso granulare via `@PreAuthorize`
- **Security Headers**: X-Frame-Options, X-Content-Type-Options, X-XSS-Protection, CSP, HSTS (via `SecurityHeadersFilter`)
- **Rate Limiting**: Protezione contro attacchi brute-force (via `RateLimitingFilter`)
- **CORS Centralizzato**: Configurazione CORS in `SecurityConfig` con origini specificate (non wildcard)
- **Validazione Input**: Tutti i DTO validati con Jakarta Validation + sanitizzazione custom
- **Gestione Errori Sicura**: `GlobalExceptionHandler` non espone informazioni sensibili
- **DataLoader Sicuro**: Utenti di test creati **solo** con profilo `dev` attivo (OWASP best practice)

### Protezione Race Condition Prenotazioni:
Il sistema implementa una **doppia protezione** contro prenotazioni concorrenti dello stesso posto:
1. **Isolation Level SERIALIZABLE** sulla transazione
2. **Locking Pessimistico** sui posti richiesti
3. **Vincolo UNIQUE** su `booked_seats(screening_id, seat_id)`
4. **Gestione DataIntegrityViolationException** come fallback

## 👤 Utenti Pre-configurati (Solo Ambiente Dev)

Gli utenti di test vengono creati **solo** quando l'applicazione viene avviata con profilo `dev`:

| Ruolo | Email | Password |
|-------|-------|----------|
| ADMIN | admin@cinema.com | admin123 |
| MANAGER | manager@cinema.com | manager123 |
| CUSTOMER | customer@example.com | customer123 |
| CUSTOMER | user@example.com | user123 |

**Per attivare il profilo dev:**
```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

Oppure impostare la variabile d'ambiente:
```bash
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

> ⚠️ **Sicurezza**: In produzione, NON attivare il profilo `dev` per evitare la creazione di utenti con password note.

## 🚀 Setup e Configurazione

### 1. Clonare il Repository

```bash
git clone <repository-url>
cd Esame-Back-End
```

### 2. Configurare Database PostgreSQL

**Metodo RAPIDO con pgAdmin:**

1. Apri **pgAdmin**
2. Connettiti al server PostgreSQL
3. Click destro su **"Databases"** → **"Create"** → **"Database..."**
4. Nome database: `Cinema`
5. Click **"Save"**

### 3. Configurare Variabili d'Ambiente

Creare il file `env.properties` nella root del progetto (copiando da `env.properties.example`):

```properties
# Database Configuration - PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/Cinema
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-change-this-in-production-minimum-32-characters
JWT_EXPIRATION=86400000

# Cloudinary Configuration (per upload immagini)
CLOUDINARY_CLOUD_NAME=your-cloudinary-cloud-name
CLOUDINARY_API_KEY=your-cloudinary-api-key
CLOUDINARY_API_SECRET=your-cloudinary-api-secret

# Mailgun Configuration (per invio email notifiche)
MAILGUN_ENABLED=true
MAILGUN_API_KEY=your-mailgun-api-key
MAILGUN_DOMAIN=your-mailgun-domain.mailgun.org
MAILGUN_FROM_EMAIL=noreply@your-domain.com
MAILGUN_FROM_NAME=Cinema Multi-Sala

# Server Configuration
SERVER_PORT=8080

# Profile (dev per dati di test, vuoto per produzione)
SPRING_PROFILES_ACTIVE=dev
```

### 4. Avviare l'Applicazione

**Sviluppo (con dati di test):**
```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

**Produzione (senza dati di test):**
```bash
./mvnw spring-boot:run
```

L'applicazione sarà disponibile su: `http://localhost:8080`

## 🧪 Test

Il progetto include una suite di test per le funzionalità critiche.

### Eseguire i Test

```bash
./mvnw test
```

### Test Implementati

**BookingServiceTest** (10 test):
- ✅ Creazione prenotazione con successo
- ✅ Errore per posti già prenotati  
- ✅ Ri-prenotazione posti dopo cancellazione
- ✅ Errore per customer non trovato
- ✅ Errore per screening non trovato
- ✅ Errore per screening non attivo
- ✅ Calcolo corretto del totale per più posti
- ✅ Recupero prenotazione per codice
- ✅ Conteggio posti prenotati per proiezione
- ✅ Calcolo ricavi per proiezione

### Configurazione Test

I test utilizzano un database H2 in-memory configurato in `src/test/resources/application-test.properties`.

## 📡 API Endpoints

### Autenticazione (`/api/auth`)
- `POST /api/auth/register` - Registrazione nuovo utente
- `POST /api/auth/login` - Login e ottenimento JWT token
- `POST /api/auth/logout` - Logout e invalidazione token

### Utenti (`/api/users`)
- `GET /api/users/me` - Ottieni profilo utente corrente
- `PUT /api/users/me` - Aggiorna profilo utente
- `POST /api/users/me/profile-image` - Upload immagine profilo (Cloudinary)

### Film (`/api/movies`)
- `GET /api/movies` - Lista tutti i film
- `GET /api/movies/{id}` - Dettagli film
- `GET /api/movies/genre/{genre}` - Film per genere
- `POST /api/movies` - Crea nuovo film (ADMIN/MANAGER)
- `PUT /api/movies/{id}` - Aggiorna film (ADMIN/MANAGER)
- `DELETE /api/movies/{id}` - Elimina film (ADMIN)
- `POST /api/movies/{id}/poster` - Upload locandina (Cloudinary)

### Sale (`/api/cinema-halls`)
- `GET /api/cinema-halls` - Lista tutte le sale
- `GET /api/cinema-halls/{id}` - Dettagli sala
- `POST /api/cinema-halls` - Crea nuova sala (ADMIN/MANAGER)
- `PUT /api/cinema-halls/{id}` - Aggiorna sala (ADMIN/MANAGER)
- `DELETE /api/cinema-halls/{id}` - Elimina sala (ADMIN)

### Proiezioni (`/api/screenings`)
- `GET /api/screenings` - Lista tutte le proiezioni
- `GET /api/screenings/{id}` - Dettagli proiezione
- `POST /api/screenings/search` - Ricerca proiezioni (filtri)
- `POST /api/screenings` - Crea nuova proiezione (ADMIN/MANAGER) → **Invia notifica email a tutti i clienti**
- `PUT /api/screenings/{id}` - Aggiorna proiezione (ADMIN/MANAGER)
- `DELETE /api/screenings/{id}` - Elimina proiezione (ADMIN)

### Prenotazioni (`/api/bookings`)
- `GET /api/bookings` - Lista prenotazioni (ADMIN/MANAGER)
- `GET /api/bookings/{id}` - Dettagli prenotazione
- `GET /api/bookings/code/{code}` - Prenotazione per codice
- `GET /api/bookings/customer/{customerId}` - Prenotazioni cliente
- `POST /api/bookings` - Crea prenotazione (CUSTOMER) → **Invia email conferma**
- `PUT /api/bookings/{id}/cancel` - Cancella prenotazione → **Invia email cancellazione** + **Libera i posti**
- `GET /api/bookings/screening/{screeningId}/booked-seats` - Posti occupati (pubblico)
- `GET /api/bookings/screening/{screeningId}/revenue` - Incasso proiezione (ADMIN/MANAGER)
- `GET /api/bookings/movie/{movieId}/revenue` - Incasso per film (ADMIN/MANAGER)

### Recensioni (`/api/reviews`)
- `GET /api/reviews` - Lista recensioni
- `GET /api/reviews/{id}` - Dettagli recensione
- `GET /api/reviews/movie/{movieId}` - Recensioni per film
- `GET /api/reviews/movie/{movieId}/average-rating` - Voto medio film
- `POST /api/reviews` - Crea recensione (CUSTOMER)
- `PUT /api/reviews/{id}` - Aggiorna recensione
- `DELETE /api/reviews/{id}` - Elimina recensione

### Email (`/api/email`) - Solo ADMIN
- `GET /api/email/verify` - Verifica configurazione Mailgun
- `POST /api/email/test?to=email@example.com` - Invia email di test

## 🔑 Autenticazione

Tutti gli endpoint protetti richiedono un header `Authorization` con il token JWT:

```
Authorization: Bearer <your-jwt-token>
```

## 📧 Integrazioni API Terze Parti

### 1. Cloudinary (Upload Immagini)
- Upload immagini profilo utente
- Upload locandine film
- Ottimizzazione automatica immagini
- Trasformazioni on-the-fly

### 2. Mailgun (Invio Email)
Il sistema invia automaticamente email via Mailgun API per:

| Evento | Email |
|--------|-------|
| Nuova proiezione creata | Notifica a tutti i clienti attivi |
| Prenotazione effettuata | Conferma con codice prenotazione |
| Prenotazione cancellata | Conferma cancellazione |

**Template email inclusi:**
- 🎬 Notifica nuova proiezione disponibile
- ✅ Conferma prenotazione con dettagli
- ❌ Conferma cancellazione prenotazione

## 📊 Query Complesse Implementate

### Filtri e Ordinamento:
- Ricerca proiezioni per data, genere, disponibilità posti
- Film ordinati per rating
- Proiezioni future con posti disponibili

### Aggregazioni:
- Conteggio posti occupati per proiezione
- Calcolo incasso totale per proiezione
- Calcolo incasso totale per film
- Voto medio recensioni per film

### Query con Locking:
- `findSeatsByIdsAndHallWithLock` - Lock pessimistico sui posti durante prenotazione
- `findAvailableSeatsForScreeningWithLock` - Verifica disponibilità con lock

## 🛠️ Tecnologie Utilizzate

- **Spring Boot 4.0.0**
- **Spring Security** (JWT + BCrypt)
- **Spring Data JPA**
- **PostgreSQL**
- **H2** (per test)
- **Lombok**
- **Jakarta Validation**
- **Cloudinary SDK** (immagini)
- **Mailgun API** (email)
- **JWT (jjwt 0.12.3)**
- **Maven**
- **JUnit 5** (test)

## 📦 Struttura Progetto

```
src/
├── main/
│   ├── java/Esame/Back_End/Esame/Back_End/
│   │   ├── config/          # Configurazioni (Cloudinary, DataLoader, Filters)
│   │   ├── controller/      # REST Controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Exception handlers (GlobalExceptionHandler)
│   │   ├── model/           # Entity JPA (12 entità)
│   │   ├── repository/      # Repository JPA (12 repository)
│   │   ├── security/        # Security (JWT, UserDetails, SecurityConfig)
│   │   ├── service/         # Business logic (10 services)
│   │   └── validation/      # Custom validators (Password, InputSanitizer)
│   └── resources/
│       ├── application.properties
│       └── static/          # Frontend UI minimale
└── test/
    ├── java/Esame/Back_End/Esame/Back_End/
    │   └── service/         # Test unitari
    └── resources/
        └── application-test.properties
```

## 📄 Postman Collection

**✅ Postman Collection presente e completa!**

Il file `Cinema_API.postman_collection.json` contiene tutte le richieste per testare l'applicazione.

**Come usare:**
1. Importa `Cinema_API.postman_collection.json` in Postman
2. Avvia l'applicazione (`./mvnw spring-boot:run -Dspring.profiles.active=dev`)
3. Esegui prima il **Login** per ottenere il token JWT
4. Il token viene salvato automaticamente per le richieste successive

**La collection include:**
- ✅ Tutti gli endpoint con esempi di payload
- ✅ Headers e parametri configurati
- ✅ Variabili per baseUrl e token JWT
- ✅ Script per auto-save del token al login

## 🐛 Troubleshooting

### Problemi di Connessione Database
- Verificare che PostgreSQL sia in esecuzione
- Controllare credenziali in `env.properties`
- Verificare che il database `Cinema` esista

### Errori JWT
- Verificare che `JWT_SECRET` sia configurato (minimo 32 caratteri)
- Controllare che il token non sia scaduto
- Verificare che il token non sia nella blacklist

### Errori Cloudinary
- Verificare che le API key siano corrette in `env.properties`

### Errori Mailgun
- Verificare che il dominio Mailgun sia verificato
- Controllare che l'API key sia corretta
- In sandbox mode, verificare che l'email destinatario sia autorizzato

### Utenti di Test Non Creati
- Assicurarsi di avviare con profilo `dev`: `-Dspring.profiles.active=dev`

## 👨‍💻 Autore

Sviluppato come progetto di esame finale Back-End

## 📄 Licenza

Questo progetto è sviluppato per scopi educativi.
