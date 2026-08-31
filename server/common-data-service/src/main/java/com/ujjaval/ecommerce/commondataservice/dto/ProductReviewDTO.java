// This is slingshot version 3.2.0
package com.ujjaval.ecommerce.commondataservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewDTO implements Serializable {
    
    private Long id;
    private Integer productId;
    private Integer userId;
    private String username;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean verifiedPurchase;
    private Integer helpfulCount;
    private Date createdDate;
    private Date updatedDate;
}
