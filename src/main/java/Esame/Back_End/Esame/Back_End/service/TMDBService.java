package Esame.Back_End.Esame.Back_End.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class TMDBService {
    
    @Value("${tmdb.api-key}")
    private String apiKey;
    
    @Value("${tmdb.base-url}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;
    
    public TMDBService() {
        this.restTemplate = new RestTemplate();
    }
    
    public Map<String, Object> getMovieDetails(Integer tmdbId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/" + tmdbId)
                .queryParam("api_key", apiKey)
                .toUriString();
        
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }
    
    public Map<String, Object> searchMovies(String query) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("language", "it-IT")
                .toUriString();
        
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }
    
    public Map<String, Object> getPopularMovies() {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/popular")
                .queryParam("api_key", apiKey)
                .queryParam("language", "it-IT")
                .queryParam("page", 1)
                .toUriString();
        
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }
    
    public String getPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }
    
    public String getBackdropUrl(String backdropPath) {
        if (backdropPath == null || backdropPath.isEmpty()) {
            return null;
        }
        return "https://image.tmdb.org/t/p/w1280" + backdropPath;
    }
}
