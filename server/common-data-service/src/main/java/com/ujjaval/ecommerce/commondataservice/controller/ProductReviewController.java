// This is slingshot version 3.2.0
package com.ujjaval.ecommerce.commondataservice.controller;

import com.ujjaval.ecommerce.commondataservice.dto.ProductReviewDTO;
import com.ujjaval.ecommerce.commondataservice.model.ProductReviewRequest;
import com.ujjaval.ecommerce.commondataservice.service.interfaces.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing product reviews.
 * Provides endpoints for CRUD operations and review statistics.
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductReviewController {

    @Autowired
    private ProductReviewService reviewService;

    /**
     * Create a new product review
     * Requires authentication via JWT token in header
     */
    @PostMapping
    public ResponseEntity<?> createReview(
            @Valid @RequestBody ProductReviewRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestHeader(value = "X-Username", required = false) String username) {
        
        try {
            // Validate authentication headers
            if (userId == null || username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required. Please login to submit a review."));
            }

            ProductReviewDTO review = reviewService.createReview(request, userId, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to create review: " + e.getMessage()));
        }
    }

    /**
     * Update an existing review
     * Requires authentication and ownership verification
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ProductReviewRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            ProductReviewDTO review = reviewService.updateReview(reviewId, request, userId);
            return ResponseEntity.ok(review);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update review: " + e.getMessage()));
        }
    }

    /**
     * Delete a review
     * Requires authentication and ownership verification
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
            }

            reviewService.deleteReview(reviewId, userId);
            return ResponseEntity.ok(createSuccessResponse("Review deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to delete review: " + e.getMessage()));
        }
    }

    /**
     * Get all reviews for a specific product with pagination
     * GET /api/reviews/product/{productId}?page=0&size=10&sort=createdDate,desc
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate,desc") String[] sort) {
        
        try {
            // Parse sort parameters
            String sortField = sort[0];
            String sortDirection = sort.length > 1 ? sort[1] : "desc";
            
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            
            Page<ProductReviewDTO> reviews = reviewService.getReviewsByProductId(productId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews.getContent());
            response.put("currentPage", reviews.getNumber());
            response.put("totalItems", reviews.getTotalElements());
            response.put("totalPages", reviews.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch reviews: " + e.getMessage()));
        }
    }

    /**
     * Get all reviews by a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReviewsByUser(@PathVariable Integer userId) {
        try {
            List<ProductReviewDTO> reviews = reviewService.getReviewsByUserId(userId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch user reviews: " + e.getMessage()));
        }
    }

    /**
     * Get review statistics for a product
     * Returns average rating, total reviews, and rating distribution
     */
    @GetMapping("/product/{productId}/statistics")
    public ResponseEntity<?> getReviewStatistics(@PathVariable Integer productId) {
        try {
            Map<String, Object> statistics = reviewService.getReviewStatistics(productId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch review statistics: " + e.getMessage()));
        }
    }

    /**
     * Mark a review as helpful
     */
    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<?> markReviewAsHelpful(@PathVariable Long reviewId) {
        try {
            ProductReviewDTO review = reviewService.markReviewAsHelpful(reviewId);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to mark review as helpful: " + e.getMessage()));
        }
    }

    /**
     * Check if user has reviewed a product
     */
    @GetMapping("/product/{productId}/user/{userId}/exists")
    public ResponseEntity<?> hasUserReviewedProduct(
            @PathVariable Integer productId,
            @PathVariable Integer userId) {
        try {
            boolean hasReviewed = reviewService.hasUserReviewedProduct(productId, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("hasReviewed", hasReviewed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to check review status: " + e.getMessage()));
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    /**
     * Helper method to create success response
     */
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> success = new HashMap<>();
        success.put("message", message);
        return success;
    }
}
