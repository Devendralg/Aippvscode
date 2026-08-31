// This is slingshot version 3.2.0
package com.ujjaval.ecommerce.commondataservice.service;

import com.ujjaval.ecommerce.commondataservice.dao.sql.ProductReviewRepository;
import com.ujjaval.ecommerce.commondataservice.dao.sql.info.ProductInfoRepository;
import com.ujjaval.ecommerce.commondataservice.dto.ProductReviewDTO;
import com.ujjaval.ecommerce.commondataservice.entity.sql.ProductReview;
import com.ujjaval.ecommerce.commondataservice.entity.sql.info.ProductInfo;
import com.ujjaval.ecommerce.commondataservice.model.ProductReviewRequest;
import com.ujjaval.ecommerce.commondataservice.service.interfaces.ProductReviewService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductReviewDTO createReview(ProductReviewRequest request, Integer userId, String username) {
        // Check if product exists
        Optional<ProductInfo> productOptional = productInfoRepository.findById(request.getProductId());
        if (!productOptional.isPresent()) {
            throw new IllegalArgumentException("Product not found with ID: " + request.getProductId());
        }

        // Check if user has already reviewed this product
        if (hasUserReviewedProduct(request.getProductId(), userId)) {
            throw new IllegalStateException("User has already reviewed this product");
        }

        ProductInfo product = productOptional.get();
        
        // Create new review
        ProductReview review = new ProductReview(
            product,
            userId,
            username,
            request.getRating(),
            request.getTitle(),
            request.getComment(),
            false // Initially set as non-verified, can be updated based on order history
        );

        ProductReview savedReview = reviewRepository.save(review);
        
        // Update product average rating
        updateProductAverageRating(request.getProductId());
        
        return convertToDTO(savedReview);
    }

    @Override
    public ProductReviewDTO updateReview(Long reviewId, ProductReviewRequest request, Integer userId) {
        Optional<ProductReview> reviewOptional = reviewRepository.findById(reviewId);
        
        if (!reviewOptional.isPresent()) {
            throw new IllegalArgumentException("Review not found with ID: " + reviewId);
        }

        ProductReview review = reviewOptional.get();
        
        // Verify that the user owns this review
        if (!review.getUserId().equals(userId)) {
            throw new SecurityException("User is not authorized to update this review");
        }

        // Update review fields
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());

        ProductReview updatedReview = reviewRepository.save(review);
        
        // Update product average rating
        updateProductAverageRating(review.getProduct().getId());
        
        return convertToDTO(updatedReview);
    }

    @Override
    public void deleteReview(Long reviewId, Integer userId) {
        Optional<ProductReview> reviewOptional = reviewRepository.findById(reviewId);
        
        if (!reviewOptional.isPresent()) {
            throw new IllegalArgumentException("Review not found with ID: " + reviewId);
        }

        ProductReview review = reviewOptional.get();
        
        // Verify that the user owns this review
        if (!review.getUserId().equals(userId)) {
            throw new SecurityException("User is not authorized to delete this review");
        }

        Integer productId = review.getProduct().getId();
        reviewRepository.delete(review);
        
        // Update product average rating
        updateProductAverageRating(productId);
    }

    @Override
    public Page<ProductReviewDTO> getReviewsByProductId(Integer productId, Pageable pageable) {
        Page<ProductReview> reviewsPage = reviewRepository.findByProductId(productId, pageable);
        return reviewsPage.map(this::convertToDTO);
    }

    @Override
    public List<ProductReviewDTO> getReviewsByUserId(Integer userId) {
        List<ProductReview> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getReviewStatistics(Integer productId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // Get average rating
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        statistics.put("averageRating", averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);
        
        // Get total review count
        Long totalReviews = reviewRepository.countByProductId(productId);
        statistics.put("totalReviews", totalReviews);
        
        // Get rating distribution
        List<Object[]> distribution = reviewRepository.findRatingDistributionByProductId(productId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        
        // Initialize all ratings with 0 count
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        
        // Fill in actual counts
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(rating, count);
        }
        
        statistics.put("ratingDistribution", ratingDistribution);
        
        return statistics;
    }

    @Override
    public ProductReviewDTO markReviewAsHelpful(Long reviewId) {
        Optional<ProductReview> reviewOptional = reviewRepository.findById(reviewId);
        
        if (!reviewOptional.isPresent()) {
            throw new IllegalArgumentException("Review not found with ID: " + reviewId);
        }

        ProductReview review = reviewOptional.get();
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        
        ProductReview updatedReview = reviewRepository.save(review);
        return convertToDTO(updatedReview);
    }

    @Override
    public boolean hasUserReviewedProduct(Integer productId, Integer userId) {
        return reviewRepository.findByProductIdAndUserId(productId, userId).isPresent();
    }

    /**
     * Update the average rating of a product based on all its reviews
     */
    private void updateProductAverageRating(Integer productId) {
        Optional<ProductInfo> productOptional = productInfoRepository.findById(productId);
        
        if (productOptional.isPresent()) {
            ProductInfo product = productOptional.get();
            Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
            
            if (averageRating != null) {
                product.setRatings(averageRating.floatValue());
                productInfoRepository.save(product);
            }
        }
    }

    /**
     * Convert ProductReview entity to DTO
     */
    private ProductReviewDTO convertToDTO(ProductReview review) {
        ProductReviewDTO dto = modelMapper.map(review, ProductReviewDTO.class);
        dto.setProductId(review.getProduct().getId());
        return dto;
    }
}
