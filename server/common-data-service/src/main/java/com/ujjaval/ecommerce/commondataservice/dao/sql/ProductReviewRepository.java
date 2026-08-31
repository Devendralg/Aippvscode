// This is slingshot version 3.2.0
package com.ujjaval.ecommerce.commondataservice.dao.sql;

import com.ujjaval.ecommerce.commondataservice.entity.sql.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    
    /**
     * Find all reviews for a specific product with pagination
     */
    Page<ProductReview> findByProductId(Integer productId, Pageable pageable);
    
    /**
     * Find all reviews by a specific user
     */
    List<ProductReview> findByUserId(Integer userId);
    
    /**
     * Find a specific review by product and user
     */
    Optional<ProductReview> findByProductIdAndUserId(Integer productId, Integer userId);
    
    /**
     * Count total reviews for a product
     */
    Long countByProductId(Integer productId);
    
    /**
     * Calculate average rating for a product
     */
    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Integer productId);
    
    /**
     * Get rating distribution for a product
     */
    @Query("SELECT pr.rating, COUNT(pr) FROM ProductReview pr WHERE pr.product.id = :productId GROUP BY pr.rating ORDER BY pr.rating DESC")
    List<Object[]> findRatingDistributionByProductId(@Param("productId") Integer productId);
    
    /**
     * Find verified purchase reviews
     */
    Page<ProductReview> findByProductIdAndVerifiedPurchaseTrue(Integer productId, Pageable pageable);
}
