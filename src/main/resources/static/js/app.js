// Cinema Multi-Sala - Simple Frontend
console.log('🚀 Script caricato!');
const API = '/api';

// Get/Set token
function getToken() { return localStorage.getItem('token'); }
function setToken(t) { localStorage.setItem('token', t); }
function getUser() { return JSON.parse(localStorage.getItem('user') || 'null'); }
function setUser(u) { localStorage.setItem('user', JSON.stringify(u)); }

// API call helper
async function api(endpoint, options = {}) {
    const headers = { 'Content-Type': 'application/json' };
    if (getToken()) headers['Authorization'] = 'Bearer ' + getToken();
    
    const res = await fetch(API + endpoint, { ...options, headers });
    const text = await res.text();
    const data = text ? JSON.parse(text) : {};
    
    if (!res.ok) throw new Error(data.message || 'Errore ' + res.status);
    return data;
}

// Show/hide sections
function show(id) {
    document.querySelectorAll('.section').forEach(s => s.classList.add('hidden'));
    document.getElementById(id).classList.remove('hidden');
}

// Login
async function doLogin(e) {
    e.preventDefault();
    try {
        const email = document.getElementById('login-email').value;
        const pwd = document.getElementById('login-password').value;
        const data = await api('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password: pwd })
        });
        setToken(data.token);
        setUser({ id: data.id, email: data.email, role: data.role });
        showDashboard();
    } catch (err) {
        alert('Errore login: ' + err.message);
    }
}

// Logout
function doLogout() {
    localStorage.clear();
    show('login-section');
}

// Show Dashboard
function showDashboard() {
    const user = getUser();
    document.getElementById('user-display').textContent = user.email + ' (' + user.role + ')';
    show('dashboard-section');
    loadScreenings();
}

// Load Screenings
async function loadScreenings() {
    const content = document.getElementById('content');
    content.innerHTML = '<p>Caricamento...</p>';
    
    try {
        const screenings = await api('/screenings');
        const movies = await api('/movies');
        const movieMap = {};
        movies.forEach(m => movieMap[m.id] = m.title);
        
        let html = '<h2>📅 Proiezioni Disponibili</h2>';
        html += '<div class="list">';
        
        for (const s of screenings) {
            const date = new Date(s.startTime).toLocaleString('it-IT');
            html += `
                <div class="item">
                    <div>
                        <strong>${movieMap[s.movieId] || 'Film #' + s.movieId}</strong><br>
                        📅 ${date} | 💰 €${s.ticketPrice}
                    </div>
                    <button onclick="prenota(${s.id}, ${s.cinemaHallId}, ${s.ticketPrice})">🎟️ Prenota</button>
                </div>
            `;
        }
        html += '</div>';
        content.innerHTML = html;
    } catch (err) {
        content.innerHTML = '<p style="color:red">Errore: ' + err.message + '</p>';
    }
}

// Prenota
async function prenota(screeningId, hallId, price) {
    const numSeats = prompt('Quanti posti vuoi prenotare? (1-5)', '2');
    if (!numSeats) return;
    
    const n = parseInt(numSeats);
    if (isNaN(n) || n < 1 || n > 5) {
        alert('Inserisci un numero tra 1 e 5');
        return;
    }
    
    // Calculate seat IDs based on hall
    const baseId = ((hallId - 1) * 150) + 50;
    const seatIds = [];
    for (let i = 0; i < n; i++) seatIds.push(baseId + i + Math.floor(Math.random() * 50));
    
    try {
        const result = await api('/bookings', {
            method: 'POST',
            body: JSON.stringify({
                customerId: getUser().id,
                screeningId: screeningId,
                seatIds: seatIds
            })
        });
        alert('✅ Prenotazione confermata!\n\nCodice: ' + result.bookingCode + '\nTotale: €' + result.totalAmount);
        loadMyBookings();
    } catch (err) {
        alert('❌ Errore: ' + err.message);
    }
}

// Load my bookings
async function loadMyBookings() {
    const content = document.getElementById('content');
    content.innerHTML = '<p>Caricamento...</p>';
    
    try {
        const user = getUser();
        const bookings = await api('/bookings/customer/' + user.id);
        
        let html = '<h2>🎟️ Le Mie Prenotazioni</h2>';
        html += '<div class="list">';
        
        if (bookings.length === 0) {
            html += '<p>Nessuna prenotazione</p>';
        } else {
            for (const b of bookings) {
                html += `
                    <div class="item">
                        <div>
                            <strong>Codice: ${b.bookingCode}</strong><br>
                            💰 €${b.totalAmount} | 📊 ${b.status}
                        </div>
                        ${b.status === 'CONFIRMED' ? `<button onclick="cancellaPrenotazione(${b.id})">❌ Cancella</button>` : ''}
                    </div>
                `;
            }
        }
        html += '</div>';
        content.innerHTML = html;
    } catch (err) {
        content.innerHTML = '<p style="color:red">Errore: ' + err.message + '</p>';
    }
}

// Cancel booking
async function cancellaPrenotazione(id) {
    if (!confirm('Vuoi cancellare questa prenotazione?')) return;
    
    try {
        await api('/bookings/' + id + '/cancel', { method: 'PUT' });
        alert('Prenotazione cancellata');
        loadMyBookings();
    } catch (err) {
        alert('Errore: ' + err.message);
    }
}

// Load movies
async function loadMovies() {
    const content = document.getElementById('content');
    content.innerHTML = '<p>Caricamento...</p>';
    
    try {
        const movies = await api('/movies');
        
        let html = '<h2>🎬 Film</h2>';
        html += '<div class="list">';
        
        for (const m of movies) {
            html += `
                <div class="item">
                    <div>
                        <strong>${m.title}</strong><br>
                        🎭 ${m.genre} | ⏱️ ${m.duration} min | ⭐ ${m.rating || 'N/D'}
                    </div>
                </div>
            `;
        }
        html += '</div>';
        content.innerHTML = html;
    } catch (err) {
        content.innerHTML = '<p style="color:red">Errore: ' + err.message + '</p>';
    }
}

// Init
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('login-form').onsubmit = doLogin;
    document.getElementById('logout-btn').onclick = doLogout;
    document.getElementById('nav-screenings').onclick = loadScreenings;
    document.getElementById('nav-bookings').onclick = loadMyBookings;
    document.getElementById('nav-movies').onclick = loadMovies;
    
    // Auto-login if token exists
    if (getToken() && getUser()) {
        showDashboard();
    }
    
    console.log('✅ App loaded');
});

// Global functions for onclick
window.prenota = prenota;
window.cancellaPrenotazione = cancellaPrenotazione;
