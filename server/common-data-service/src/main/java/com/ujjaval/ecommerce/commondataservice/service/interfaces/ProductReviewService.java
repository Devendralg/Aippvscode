// This is slingshot version 3.2.0
package com.ujjaval.ecommerce.commondataservice.service.interfaces;

import com.ujjaval.ecommerce.commondataservice.dto.ProductReviewDTO;
import com.ujjaval.ecommerce.commondataservice.model.ProductReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing product reviews.
 */
public interface ProductReviewService {
    
    /**
     * Create a new product review
     * @param request Review request containing product ID, rating, title, and comment
     * @param userId ID of the user creating the review
     * @param username Username of the reviewer
     * @return Created review DTO
     */
    ProductReviewDTO createReview(ProductReviewRequest request, Integer userId, String username);
    
    /**
     * Update an existing review
     * @param reviewId ID of the review to update
     * @param request Updated review data
     * @param userId ID of the user updating the review
     * @return Updated review DTO
     */
    ProductReviewDTO updateReview(Long reviewId, ProductReviewRequest request, Integer userId);
    
    /**
     * Delete a review
     * @param reviewId ID of the review to delete
     * @param userId ID of the user deleting the review
     */
    void deleteReview(Long reviewId, Integer userId);
    
    /**
     * Get all reviews for a product with pagination
     * @param productId Product ID
     * @param pageable Pagination information
     * @return Page of review DTOs
     */
    Page<ProductReviewDTO> getReviewsByProductId(Integer productId, Pageable pageable);
    
    /**
     * Get reviews by user
     * @param userId User ID
     * @return List of review DTOs
     */
    List<ProductReviewDTO> getReviewsByUserId(Integer userId);
    
    /**
     * Get review statistics for a product
     * @param productId Product ID
     * @return Map containing average rating, total reviews, and rating distribution
     */
    Map<String, Object> getReviewStatistics(Integer productId);
    
    /**
     * Mark a review as helpful
     * @param reviewId Review ID
     * @return Updated review DTO
     */
    ProductReviewDTO markReviewAsHelpful(Long reviewId);
    
    /**
     * Check if user has already reviewed a product
     * @param productId Product ID
     * @param userId User ID
     * @return true if user has reviewed the product, false otherwise
     */
    boolean hasUserReviewedProduct(Integer productId, Integer userId);
}
