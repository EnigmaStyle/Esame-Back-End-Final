package Esame.Back_End.Esame.Back_End.service;

import Esame.Back_End.Esame.Back_End.dto.ReviewDTO;
import Esame.Back_End.Esame.Back_End.exception.BadRequestException;
import Esame.Back_End.Esame.Back_End.exception.ResourceNotFoundException;
import Esame.Back_End.Esame.Back_End.model.Customer;
import Esame.Back_End.Esame.Back_End.model.Movie;
import Esame.Back_End.Esame.Back_End.model.Review;
import Esame.Back_End.Esame.Back_End.repository.CustomerRepository;
import Esame.Back_End.Esame.Back_End.repository.MovieRepository;
import Esame.Back_End.Esame.Back_End.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final MovieRepository movieRepository;
    
    public ReviewService(ReviewRepository reviewRepository, CustomerRepository customerRepository,
                        MovieRepository movieRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
        this.movieRepository = movieRepository;
    }
    
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return convertToDTO(review);
    }
    
    public List<ReviewDTO> getReviewsByMovieId(Long movieId) {
        return reviewRepository.findByMovieId(movieId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<ReviewDTO> getReviewsByCustomerId(Long customerId) {
        return reviewRepository.findByCustomerId(customerId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ReviewDTO createReview(ReviewDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        
        Movie movie = movieRepository.findById(dto.getMovieId())
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + dto.getMovieId()));
        
        // Check if customer already reviewed this movie
        if (reviewRepository.findByCustomerIdAndMovieId(dto.getCustomerId(), dto.getMovieId()).isPresent()) {
            throw new BadRequestException("Customer has already reviewed this movie");
        }
        
        Review review = Review.builder()
            .rating(dto.getRating())
            .comment(dto.getComment())
            .customer(customer)
            .movie(movie)
            .build();
        
        Review saved = reviewRepository.save(review);
        return convertToDTO(saved);
    }
    
    @Transactional
    public ReviewDTO updateReview(Long id, ReviewDTO dto) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        
        if (dto.getRating() != null) review.setRating(dto.getRating());
        if (dto.getComment() != null) review.setComment(dto.getComment());
        
        Review updated = reviewRepository.save(review);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }
    
    public Double getAverageRatingByMovie(Long movieId) {
        return reviewRepository.calculateAverageRatingByMovieId(movieId);
    }
    
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewDate(review.getReviewDate());
        dto.setCustomerId(review.getCustomer().getId());
        dto.setMovieId(review.getMovie().getId());
        return dto;
    }
}

