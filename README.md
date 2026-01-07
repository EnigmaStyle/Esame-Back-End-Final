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

### Credenziali di Test:
| Ruolo | Email | Password |
|-------|-------|----------|
| CUSTOMER | customer@example.com | customer123 |
| MANAGER | manager@cinema.com | manager123 |
| ADMIN | admin@cinema.com | admin123 |

---

## 🎯 Caratteristiche Principali

- **Architettura a 3 livelli**: Model, Repository, Service, Controller
- **Sicurezza JWT**: Autenticazione e autorizzazione basata su token JWT
- **Ereditarietà Database**: Strategia JOINED per la gerarchia degli utenti
- **API Terze Parti**: Integrazione con Cloudinary (immagini) e Mailgun (email)
- **Query Complesse**: Filtri, ordinamento e aggregazioni con JPQL/SQL nativo
- **Validazione Completa**: Validazione DTO con Jakarta Validation
- **Gestione Errori**: Global Exception Handler centralizzato

## 📋 Requisiti

- Java 21
- Maven 3.8+
- PostgreSQL 12+
- Account Cloudinary (per upload immagini)
- Account Mailgun (per invio email notifiche)

## 🗄️ Database Schema

Il sistema utilizza **11 tabelle** con ereditarietà:

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
10. **reviews** (Recensioni)
11. **booking_seats** (Tabella di join per prenotazioni-posti)

### Ereditarietà:
- **User** (classe astratta) con strategia `@Inheritance(strategy = InheritanceType.JOINED)`
- **Admin**, **Manager**, **Customer** estendono User

## 🔐 Sicurezza

L'applicazione implementa multiple layer di sicurezza:

- **Password Policy Forte**: Password devono contenere almeno 8 caratteri, maiuscole, minuscole, numeri e caratteri speciali
- **BCrypt**: Criptazione password con BCryptPasswordEncoder e salt automatico
- **JWT**: Token JWT per autenticazione stateless con scadenza configurata
- **RBAC**: 3 ruoli distinti (ADMIN, MANAGER, CUSTOMER) con controllo accesso granulare
- **Security Headers**: X-Frame-Options, X-Content-Type-Options, X-XSS-Protection, CSP, HSTS
- **CORS Configurato**: Origini specificate, non wildcard
- **Validazione Input**: Tutti i DTO validati con Jakarta Validation
- **Gestione Errori Sicura**: Errori non espongono informazioni sensibili

## 👤 Utenti Pre-configurati

All'avvio dell'applicazione, vengono creati automaticamente i seguenti utenti di esempio:

| Ruolo | Email | Password |
|-------|-------|----------|
| ADMIN | admin@cinema.com | admin123 |
| MANAGER | manager@cinema.com | manager123 |
| CUSTOMER | customer@example.com | customer123 |
| CUSTOMER | user@example.com | user123 |

**Nota**: Questi utenti vengono creati solo se non esistono già nel database.

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

Modificare il file `src/main/resources/application.properties`:

```properties
# Database Configuration - PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/Cinema
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password

# JWT Configuration
jwt.secret=your-256-bit-secret-key-change-this-in-production-minimum-32-characters
jwt.expiration=86400000

# Cloudinary Configuration (per upload immagini)
cloudinary.cloud-name=your-cloudinary-cloud-name
cloudinary.api-key=your-cloudinary-api-key
cloudinary.api-secret=your-cloudinary-api-secret

# Mailgun Configuration (per invio email notifiche)
mailgun.api-key=your-mailgun-api-key
mailgun.domain=your-mailgun-domain.mailgun.org
mailgun.from-email=noreply@your-domain.com
mailgun.from-name=Cinema Multi-Sala

# Server Configuration
server.port=8080
```

### 4. Avviare l'Applicazione

```bash
./mvnw spring-boot:run
```

L'applicazione sarà disponibile su: `http://localhost:8080`

## 📡 API Endpoints

### Autenticazione (`/api/auth`)
- `POST /api/auth/register` - Registrazione nuovo utente
- `POST /api/auth/login` - Login e ottenimento JWT token

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
- `PUT /api/bookings/{id}/cancel` - Cancella prenotazione → **Invia email cancellazione**
- `GET /api/bookings/screening/{screeningId}/booked-seats` - Posti occupati
- `GET /api/bookings/screening/{screeningId}/revenue` - Incasso proiezione
- `GET /api/bookings/movie/{movieId}/revenue` - Incasso per film

### Recensioni (`/api/reviews`)
- `GET /api/reviews` - Lista recensioni
- `GET /api/reviews/{id}` - Dettagli recensione
- `GET /api/reviews/movie/{movieId}` - Recensioni per film
- `GET /api/reviews/movie/{movieId}/average-rating` - Voto medio film
- `POST /api/reviews` - Crea recensione (CUSTOMER)
- `PUT /api/reviews/{id}` - Aggiorna recensione
- `DELETE /api/reviews/{id}` - Elimina recensione

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

## 🛠️ Tecnologie Utilizzate

- **Spring Boot 4.0.0**
- **Spring Security** (JWT + BCrypt)
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Jakarta Validation**
- **Cloudinary SDK** (immagini)
- **Mailgun API** (email)
- **JWT (jjwt)**
- **Maven**

## 📦 Struttura Progetto

```
src/main/java/Esame/Back_End/Esame/Back_End/
├── config/          # Configurazioni (Cloudinary, DataLoader, Security)
├── controller/      # REST Controllers
├── dto/             # Data Transfer Objects
├── exception/       # Exception handlers
├── model/           # Entity JPA
├── repository/      # Repository JPA
├── security/        # Security (JWT, UserDetails, etc.)
├── service/         # Business logic (incl. MailgunService, CloudinaryService)
└── validation/      # Custom validators
```

## 📄 Postman Collection

**✅ Postman Collection presente e completa!**

Il file `Cinema_API.postman_collection.json` contiene tutte le richieste per testare l'applicazione.

**Come usare:**
1. Importa `Cinema_API.postman_collection.json` in Postman
2. Avvia l'applicazione (`./mvnw spring-boot:run`)
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
- Controllare credenziali in `application.properties`
- Verificare che il database `Cinema` esista

### Errori JWT
- Verificare che `jwt.secret` sia configurato (minimo 32 caratteri)
- Controllare che il token non sia scaduto

### Errori Cloudinary
- Verificare che le API key siano corrette in `application.properties`

### Errori Mailgun
- Verificare che il dominio Mailgun sia verificato
- Controllare che l'API key sia corretta
- In sandbox mode, verificare che l'email destinatario sia autorizzato

## 👨‍💻 Autore

Sviluppato come progetto di esame finale Back-End

## 📄 Licenza

Questo progetto è sviluppato per scopi educativi.
